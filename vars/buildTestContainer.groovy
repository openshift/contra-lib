/**
 * Build/Test/Push container to a DockerHub
 * @param parameters
 * versions: A list of versions to tag the image with. If supplied, the version will be pushed to docker hub
 * image_name: The name of the image that should be pushed to docker hub
 * credentials: Credentials for container push. Must contain USERNAME, PASSWORD as variables
 * @return
 */

def call(Map parameters = [:]) {
    def versions = parameters.versions ?: []
    def build_cmd = parameters.build_cmd ?: 'build'
    def test_cmd = parameters.test_cmd ?: 'test'
    def image_name = parameters.image_name
    def container_registry = parameters.container_registry ?: 'docker://docker.io'
    def container_namespace = parameters.container_namespace
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
        stage('Build-Container-Image') {
            def cmd = """
        set -x
        make ${build_cmd}
        """
            containerWrapper(cmd)
        }

        stage('test-container') {
            def cmd = """
        set -x
        make ${test_cmd}
        """
            containerWrapper(cmd)
        }

        versions.each { VERSION ->
            stage("Tag-Push-Image-${VERSION}") {
                def cmd = """
            set -x
            make push VERSION=${VERSION} USERNAME=\${CONTAINER_USERNAME} PASSWORD=\${CONTAINER_PASSWORD}
            """
                containerWrapper(cmd)
            }
        }
    }
}

