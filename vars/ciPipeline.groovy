/**
 * requires: buildVars['package_name']
 * optional: buildVars['displayName'], buildVars['buildDescription']
 * Example Usage:
 *
 * ciPipeline(buildPrefix: 'fedora-pipeline', buildVars: [package_name: 'vim']) {
 *     stage('run-job') {
 *         handlePipelineStep {
 *             runCode()
 *         }
 *     }
 * }
 *
 * @param parameters
 * @param body
 * @return
 */
import org.contralib.ciMetrics


def call(Map parameters, Closure body) {
    def buildPrefix = parameters.get('buildPrefix', 'contra-pipeline')
    def packageName = parameters.get('package_name')
    def failedMsg = parameters.get('failedMsg')
    def completeMsg = parameters.get('completeMsg')
    def timeoutValue = parameters.get('timeout', 30)
    def sendMetrics = parameters.get('sendMetrics', true)

    def jobMeasurement = env.JOB_NAME
    def packageMeasurement = null


    def cimetrics = ciMetrics.metricsInstance
    cimetrics.prefix = buildPrefix


    timeout(time: timeoutValue, unit: 'MINUTES') {

        try {
            body()
        } catch (e) {
            // Set build result
            currentBuild.result = "FAILURE"

            echo e.getMessage()

            if (failedMsg) {
                sendMessageWithAudit(failedMsg())
            }

            throw e
        } finally {
            currentBuild.result = currentBuild.result ?: 'SUCCESS'

            if (completeMsg) {
                sendMessageWithAudit(completeMsg())
            }

            if (currentBuild.result == 'SUCCESS') {
                step([$class     : 'ArtifactArchiver', allowEmptyArchive: true,
                      artifacts  : '**/logs/**,*.txt,*.groovy,**/job.*,**/*.groovy,**/inventory.*', excludes: '**/job.props,**/job.props.groovy,**/*.example,**/*.qcow2',
                      fingerprint: true])
            } else {
                step([$class     : 'ArtifactArchiver', allowEmptyArchive: true,
                      artifacts  : '**/logs/**,*.txt,*.groovy,**/job.*,**/*.groovy,**/inventory.*,**/*.qcow2', excludes: '**/job.props,**/job.props.groovy,**/*.example',
                      fingerprint: true])
            }

            currentBuild.displayName = currentBuild.displayName ?: "Build #${env.BUILD_NUMBER}"
            currentBuild.description = currentBuild.description ?: currentBuild.result

            if (sendMetrics) {
                pipelineMetrics(buildPrefix: buildPrefix, package_name: packageName)
            }


        }
    }

}
