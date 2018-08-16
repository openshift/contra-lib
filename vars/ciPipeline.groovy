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
 * buildPrefix: String - A unique name to your Jenkins instance. It's mainly used to prefix influx data
 * packageName: String - If set, metrics will be recorded about the package.
 * errorMsg: fedErrorMsg - a fedErrorMsg to send on job failure
 * completeMsg: fedCompleteMsg - a fedCompleteMsg to send on job completion
 * decorateBuild: Closure - A closure that will be called to decorate the build. Defaults to build_number and build result
 * postBuild: Closure -  A closure that will run any post build actions. This can be used to archive artifacts or cleanup
 *             after the build has run
 * timeoutValue: Integer - How long before the job should timeout. Defaults to 30 minutes.
 * sendMetrics: Boolean - send metrics to influx or not
 * @param body
 * @return
 */
import org.contralib.ciMetrics


def call(Map parameters = [:], Closure body) {
    def buildPrefix = parameters.get('buildPrefix', env.OPENSHIFT_BUILD_NAMESPACE)
    def packageName = parameters.get('package_name')
    def errorMsg = parameters.get('errorMsg')
    def completeMsg = parameters.get('completeMsg')
    def decorateBuild = parameters.get('decorateBuild')
    def postBuild = parameters.get('postBuild')
    def timeoutValue = parameters.get('timeout', 30)
    def sendMetrics = parameters.get('sendMetrics', true)

    if (!buildPrefix) {
        throw new RuntimeException('Must supply buildPrefix')
    }

    def cimetrics = ciMetrics.metricsInstance
    cimetrics.prefix = buildPrefix

    timeout(time: timeoutValue, unit: 'MINUTES') {

        try {
            body()
        } catch (e) {
            // Set build result
            currentBuild.result = "FAILURE"

            echo e.getMessage()

            if (errorMsg) {
                sendMessageWithAudit(errorMsg())
            }

            throw e
        } finally {

            try {
                if (postBuild) {
                    postBuild()
                }
            } catch(e) {
                echo "Exception in post build: ${e.toString()}"
                currentBuild.result = 'FAILED'
            }

            currentBuild.result = currentBuild.result ?: 'SUCCESS'

            if (sendMetrics) {
                pipelineMetrics(buildPrefix: buildPrefix, package_name: packageName)
            }

            if (completeMsg) {
                sendMessageWithAudit(completeMsg())
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
