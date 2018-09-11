/**
 * Wrapper step to print debugging information and record the time spent running the wrapped code
 * Example Usage:
 *
 * stage('mystage') {
 *     handlePipelineStep {
 *         runCode()
 *     }
 * }
 *
 * @param parameters
 * @param body
 * @return
 */
import org.centos.pipeline.ciMetrics


def call(Map parameters = [:], Closure body) {
    def measurementName = parameters.get('measurement', env.JOB_NAME)
    def name = parameters.get('stageName', env.STAGE_NAME ?: env.JOB_NAME)
    def queuedMsg = parameters.get('queuedMsg')
    def runningMsg = parameters.get('runningMsg')

    def cimetrics = ciMetrics.metricsInstance

    try {

        if (queuedMsg) {
            sendMessageWithAudit(queuedMsg())
        }

        print "running pipeline step: ${name}"
        cimetrics.timed measurementName, name, {

            if (runningMsg) {
                sendMessageWithAudit(runningMsg())
            }

            body()

        }
    } catch(e) {

        echo "${env.JOB_NAME} failed in stage: ${name} with error: ${e.getMessage()}"
        throw e
    } finally {
        print "end of pipeline step: ${name}"
    }
}
