/**
 * Build/Test/Push container to a DockerHub
 * @param parameters
 * versions: A list of versions to tag the image with. If supplied, the version will be pushed to DockerHub
 * test_cmd: The test command provided as a shell command
 * image_name: The name of the image to build
 * build_args: Any arguments to provide to the docker build process
 * modify_args: Arguments to modify the image with before testing. The format is follows
 *              [owner: 'uid:gid', items: [['srcfile1', 'destfile1'], ['srcfile2', 'destfile2']]
 * podTemplateProps: Refer to deployOpenshiftTemplate
 * credentials: Credentials for docker push. Must contain DOCKER_USERNAME, DOCKER_PASSWORD as variables
 * @return
 */

def call(Map parameters = [:]) {
    def versions = parameters.versions ?: []
    def build_cmd = parameters.build_cmd ?: 'buildcontainer'
    def test_cmd = parameters.test_cmd ?: 'testcontainer'
    def image_name = parameters.image_name
    def docker_registry = parameters.docker_registry ?: 'docker://docker.io'
    def docker_namespace = parameters.docker_namespace
    def buildContainer = parameters.buildContainer ?: 'buildah'
    def credentials = parameters.credentials ?: []
    def build_root = parameters.build_root ?: '.'

    def containerWrapper = { cmd ->
        executeInContainer(containerName: buildContainer, containerScript: cmd, stageVars: [], credentials: credentials)
    }

    stage('prepare-build') {
        handlePipelineStep {
            deleteDir()

            // include option to checkout linchpin version
            checkout scm

        }
    }

    dir(build_root) {
        stage('Build-Docker-Image') {
            def cmd = """
        set -x
        make ${build_cmd}
        """
            containerWrapper(cmd)
        }

        stage('test-docker-container') {
            def cmd = """
        make ${test_cmd}
        """
            containerWrapper(cmd)
        }

        if (versions) {
            stage('Tag-Push-docker-image') {
                def cmd = 'set -x'<<'\n'
                versions.each { version ->
                    cmd << "buildah tag ${image_name} ${image_name}:${version}"
                    cmd << "\n"

                    if (credentials) {
                        cmd << "buildah push --creds \${USERNAME}:\${PASSWORD} localhost/${image_name}:${version} ${docker_registry}/${docker_namespace}/${image_name}:${version}"
                        cmd << "\n"
                    } else {
                        cmd << "buildah push localhost/${image_name}:${version} ${docker_registry}/${docker_namespace}/${image_name}:${version}"
                        cmd << "\n"
                    }

                }

                containerWrapper(cmd)
            }
        }
    }
}

