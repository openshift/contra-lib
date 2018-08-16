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
