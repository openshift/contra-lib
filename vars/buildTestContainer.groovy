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

def call(parameters = [:]) {
    def versions = parameters.versions ?: []
    def test_cmd = parameters.test_cmd
    def image_name = parameters.image_name
    def build_args = parameters.build_args ?: [:]
    def modify_args = parameters.modify_args ?: [:]
    def docker_registry = parameters.docker_registry ?: 'docker://docker.io'
    def docker_namespace = parameters.docker_namespace
    def send_metrics = parameters.send_metrics ?: false
    def podTemplateProps = parameters.podTemplateProps ?: [:]
    def credentials = parameters.credentials ?: []
    def build_root = parameters.build_root ?: '.'
    def container_name = parameters.container_name ?: UUID.randomUUID().toString()

    def buildContainer = podTemplateProps.get('containers')
    if (buildContainer) {
        buildContainer = buildContainer[0]
    }

    deployOpenShiftTemplate(podTemplateProps) {
        ciPipeline(sendMetrics: send_metrics, decoratePRBuild: decoratePRBuild()) {


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

            stage('Build-Docker-Image') {
                def buildCmd = null

                if (build_args) {
                    def joinedArgs = build_args.collect { key, value -> "${key}=${value}"}.join(" --build-arg ")
                    buildCmd = "buildah bud --build-arg ${joinedArgs} -t ${image_name} ${build_root}"

                } else {
                    buildCmd = "buildah bud -t ${image_name} ${build_root}"
                }

                def cmd = """
                set -x
                ${buildCmd}
                buildah from --name ${container_name} ${image_name}
                """
                containerWrapper(cmd)
            }

            stage('test-docker-container') {
                def cmd = ""

                if (modify_args) {
                    def owner = modify_args['owner']
                    def copyCmds = modify_args['items'].collect { item ->
                        "buildah copy --chown ${owner} ${container_name} ${item[0]} ${item[1]}"
                    }.join('\n')

                    cmd = """
                    set -x
                    ${copyCmds}
                    buildah commit ${container_name} ${image_name}-test
                    buildah from --name ${container_name}-test ${image_name}-test
                    buildah run ${container_name}-test -- ${test_cmd}
                    """
                } else {
                    cmd = """
                    set -x
                    buildah run ${container_name} -- ${test_cmd}
                    """
                }

                containerWrapper(cmd)
            }

            if (versions) {
                stage('Tag-Push-docker-image') {
                    def cmd = 'set -x'<<'\n'
                    versions.each { version ->
                        cmd << "buildah tag ${image_name} ${image_name}:${version}"
                        cmd << "\n"

                        if (credentials) {
                            cmd << "buildah push --creds \${DOCKER_USERNAME}:\${DOCKER_PASSWORD} localhost/${image_name}:${version} ${docker_registry}/${docker_namespace}/${image_name}:${version}"
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
}
