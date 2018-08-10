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

    handlePipelineStep {
        withCredentials(credentials) {
            def localVars = [:]

            stageVars.each { key, value ->
                localVars[key] = value
            }

            loadProps.each { stage ->
                def jobProps = readProperties file: "${stage}/job.props"
                localVars << jobProps
            }

            def containerEnv = localVars.collect { key, value -> return key+'='+value }
            sh "mkdir -p ${stageName}"
            try {
                withEnv(containerEnv) {
                    container(containerName) {
                        sh containerScript
                    }
                }

            } catch (err) {
                throw err
            } finally {
                sh "[ -e ${stageName}/logs ] && mv -vf logs ${stageName}/logs || true"
                sh "[ -e ${stageName}/job.props ] && mv -vf job.props ${stageName}/job.props || true"
            }
        }
    }
}
