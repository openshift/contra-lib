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
    def stageMsg = parameters.get('stageMsg')

    def cimetrics = ciMetrics.metricsInstance

    try {

        print "running pipeline step: ${name}"
        def runtime = cimetrics.timed measurementName, name, {

            body()

            if (stageMsg) {
                def runtimeMsg = ['stage': ['runtime': runtime]]
                sendMessageWithAudit(stageMsg(runtimeMsg))
            }

        }
    } catch(e) {
        echo "${env.JOB_NAME} failed in stage: ${name} with error: ${e.toString()}"
        throw e
    } finally {
        print "end of pipeline step: ${name}"
    }
}
