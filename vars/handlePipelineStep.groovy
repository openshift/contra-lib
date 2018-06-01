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
import org.contralib.ciMetrics


def call(Map parameters = [:], Closure body) {
    def measurementName = parameters.get('measurement', env.JOB_NAME)
    def name = parameters.get('stageName', env.STAGE_NAME ?: env.JOB_NAME)
    def beforeRunMsg = parameters.get('beforeRunMsg')
    def afterRunMsg = parameters.get('afterRunMsg')
    def failedRunMsg = parameters.get('failedRunMsg')

    def cimetrics = ciMetrics.metricsInstance

    try {
        print "running pipeline step: ${name}"
        cimetrics.timed measurementName, name, {
            if (beforeRunMsg) {
                sendMessageWithAudit(beforeRunMsg())
            }

            body()

            if (afterRunMsg) {
                sendMessageWithAudit(afterRunMsg())
            }

        }
    } catch(e) {

        if (failedRunMsg) {
            sendMessageWithAudit(failedRunMsg())
        }

        throw e
    } finally {
        print "end of pipeline step: ${name}"
    }
}
