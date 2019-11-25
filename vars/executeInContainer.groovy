/**
 * Execute a script using a container
 * Example Usage:
 *
 * stageVars = ['repo': 'vim', 'branch': 'f28']
 * executeInContainer(containerName: 'rpmbuild', containerScript: 'rpmbuild.sh', stageVars: stageVars)
 *
 * @param parameters
 * @return
 */

def call(Map parameters) {
    def containerName = parameters.get('containerName')
    def containerScript = parameters.get('containerScript')
    def stageVars = parameters.get('stageVars', [:])
    def stageName = parameters.get('stageName', env.STAGE_NAME)
    def loadProps = parameters.get('loadProps', [])
    def credentials = parameters.get('credentials', [])
    def returnStdout = parameters.get('returnStdout', false)

    def stageDir = "${WORKSPACE}/${stageName}"

    handlePipelineStep {
        withCredentials(credentials) {

            def localVars = [:]

            stageVars.each { key, value ->
                localVars[key] = value
            }

            loadProps.each { stage ->
                def jobProps = readProperties file: "${stageDir}/job.props"
                localVars << jobProps
            }

            def containerEnv = localVars.collect { key, value -> return key+'='+value }

            sh script: "mkdir -p ${stageDir}", label: "Creating directory: ${stageDir}"

            try {
                withEnv(containerEnv) {
                    container(containerName) {
                        sh script: containerScript, returnStdout: returnStdout
                    }
                }

            } catch(err) {
                throw new Exception("Error running ${containerScript}: ${err.toString()}")
            } finally {
                if (fileExists("logs/")) {
                    sh script: "mv logs ${stageDir}/logs", label: "Moving logs to the ${stageDir}/logs"
                }

                if (fileExists("job.props")) {
                    sh script: "mv job.props ${stageDir}/job.props", label: "Moving job.props to the ${stageDir}/job.props"
                }
            }
        }
    }
}
