



def call(parameters = [:]) {
    def version = parameters.get('version')
    def test_cmd = parameters.get('test_cmd')
    def image_name = parameters.get('image_name')
    def docker_registry = parameters.get('docker_registry')
    def docker_namespace = parameters.get('docker_namespace')
    def podTemplateProps = parameters.get('podTemplateProps', [:])
    def credentials = parameters.get('credentials', [])
    def build_root = parameters.get('build_root', '.')
    def container_name = parameters.get('container_name', UUID.randomUUID().toString())

    def buildContainer = podTemplateProps.get('containers')
    if (buildContainer) {
        buildContainer = buildContainer[0]
    }

    deployOpenShiftTemplate(podTemplateProps) {
        ciPipeline {

            print buildContainer

            def containerWrapper = { cmd ->
                executeInContainer(containerName: buildContainer, containerScript: cmd, stageVars: [], credentials: credentials)
            }

            stage('prepare-build') {
                handlePipelineStep {
                    deleteDir()

                    checkout scm

                    currentBuild.displayName = "Build#: ${env.BUILD_NUMBER} - Container Build: ${version}"

                }
            }

            stage('Build-Docker-Image') {
                def cmd = """
            buildah bud -t ${image_name} ${build_root}
            buildah from --name ${container_name} ${image_name}
                
            """
                containerWrapper(cmd)
            }

            stage('test docker image') {
                def cmd = """
                buildah run ${container_name} -- ${test_cmd}
                """
                containerWrapper(cmd)
            }

            stage('Tag-Push-docker-image') {
                def pushCmd = null
                if (credentials) {
                    pushCmd = "buildah push --creds ${DOCKER_USERNAME}:${DOCKER_PASSWORD} localhost/${image_name}:latest ${docker_registry}/${docker_namespace}/${image_name}:latest"
                } else {
                    pushCmd = "buildah push localhost/${image_name}:latest ${docker_registry}/${docker_namespace}/${image_name}:latest"
                }
                def cmd = """
                buildah tag ${image_name} ${image_name}:latest ${image_name}:${version}
                ${pushCmd}
                """
                containerWrapper(cmd)
            }
        }
    }
}
