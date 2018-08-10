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
    def stageStatus = null
    def runtime = 0.0

    try {

        print "running pipeline step: ${name}"
        runtime = cimetrics.timed measurementName, name, {

            body()

            stageStatus = 'complete'

        }

    } catch(e) {

        stageStatus = 'error'
        echo "${env.JOB_NAME} failed in stage: ${name} with error: ${e.toString()}"
        throw e

    } finally {

        if (stageMsg) {
            def runtimeMsg = ['stage': ['runtime': runtime,
                                        'name': env.STAGE_NAME,
                                        'status': stageStatus
                                       ]
            ]
            sendMessageWithAudit(stageMsg(runtimeMsg))
        }

        print "end of pipeline step: ${name}"
    }
}
