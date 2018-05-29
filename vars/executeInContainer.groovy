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

    def containerEnv = stageVars.collect { key, value -> return key+'='+value }
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
        sh "mv -vf logs ${stageName}/logs || true"
    }

}
