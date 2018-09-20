def call(parameters = [:]) {
    def version = parameters.get('version')
    def test_cmd = parameters.get('test_cmd')
    def image_name = parameters.get('image_name')
    def containers = parameters.get('containers', [])
    def credentials = parameters.get('credentials', [])
    def build_root = parameters.get('build_root', '.')
    def container_name = parameters.get('container_name', UUID.randomUUID().toString())

    deployOpenShiftTemplate(containers: containers) {
        ciPipeline {

            def containerWrapper = { cmd ->
                executeInContainer(containerName: 'buildah', containerScript: cmd, stageVars: [], credentials: credentials)
            }

            stage('prepare build') {
                handlePipelineStep {
                    deleteDir()

                    checkout scm

                    currentBuild.displayName = "Build#: ${env.BUILD_NUMBER} - Container Build: ${version}"

                }
            }

            stage('Build Docker Image') {
                def cmd = """
            buildah bud -t ${image_name} ${build_root}
            buildah from --name ${container_name} ${image_name}
                
            """
                containerWrapper(cmd)
            }

            stage('test docker image') {
                containerWrapper(test_cmd)
            }

            stage('Tag/Push docker image') {
                def cmd = """
            buildah tag ${image_name} ${image_name}:latest ${image_name}:${version}
            buildah push --creds ${DOCKER_USERNAME}:${DOCKER_PASSWORD} localhost/${image_name}:latest ${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${image_name}:latest
            """
                containerWrapper(cmd)
            }
        }
    }
}
