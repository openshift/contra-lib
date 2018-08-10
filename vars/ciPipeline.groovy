/**
 * requires: buildPrefix
 * Example Usage:
 *
 * ciPipeline(buildPrefix: 'fedora-pipeline') {
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
    def buildPrefix = parameters.get('buildPrefix')
    def packageName = parameters.get('package_name')
    def errorMsg = parameters.get('errorMsg')
    def completeMsg = parameters.get('completeMsg')
    def decorateBuild = parameters.get('decorateBuild')
    def archiveArtifacts = parameters.get('archiveArtifacts')
    def timeoutValue = parameters.get('timeout', 30)
    def sendMetrics = parameters.get('sendMetrics', true)


    def cimetrics = ciMetrics.metricsInstance
    cimetrics.prefix = buildPrefix


    timeout(time: timeoutValue, unit: 'MINUTES') {

        try {
            body()
        } catch (e) {
            // Set build result
            currentBuild.result = "FAILURE"

            echo e.toString()

            if (errorMsg) {
                sendMessageWithAudit(errorMsg(runtime: currentBuild.getDuration()))
            }

            throw e
        } finally {
            currentBuild.result = currentBuild.result ?: 'SUCCESS'


            if (archiveArtifacts) {
                archiveArtifacts()
            }

            if (sendMetrics) {
                pipelineMetrics(buildPrefix: buildPrefix, package_name: packageName)
            }

            if (completeMsg) {
                runtime = ['pipeline': ['runtime': currentBuild.getDuration()]]
                sendMessageWithAudit(completeMsg(['pipeline': ['runtime': currentBuild.getDuration()]))
            }

            if (decorateBuild) {
                decorateBuild()
            } else {
                currentBuild.displayName = "Build #${env.BUILD_NUMBER}"
                currentBuild.description = currentBuild.result
            }

        }
    }

}
