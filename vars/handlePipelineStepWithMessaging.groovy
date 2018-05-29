/**
 * Wrapper step to print debugging information and record the time spent running the wrapped code.
 * This will also put a message on a topic before and after a stage runs. Also, if the stage fails.
 * Example Usage:
 *
 * beforeRunMsg = ['msgTopic': 'stageName', 'msgProps': 'stage=stage1', 'msgContent': '{"pipelineResult":"stage1 queued"}']
 * afterRunMsg = ['msgTopic': 'stageName', 'msgProps': 'stage=stage1', 'msgContent': '{"pipelineResult":"stage1 success"}']
 * failedRunMsg = ['msgTopic': 'stageName', 'msgProps': 'stage=stage1', 'msgContent': '{"pipelineResult":"stage failed"}']
 * stage('mystage') {
 *     handlePipelineStepWithMessaging(beforeRunMsg: beforeRunMsg,
 *                                     afterRunMsg: afterRunMsg,
 *                                     failedRunMsg: failedRunMsg) {
 *         runCode()
 *     }
 * }
 *
 * @param parameters
 * @param body
 * @return
 */


def call(Map parameters = [:], Closure body) {
    def measurementName = parameters.get('measurement', env.JOB_NAME)
    def name = parameters.get('stageName', env.STAGE_NAME ?: env.JOB_NAME)
    def beforeRunMsg = parameters.get('beforeRunMsg')
    def afterRunMsg = parameters.get('afterRunMsg')
    def failedRunMsg = parameters.get('failedRunMsg')

    try {
        print "running pipeline step: ${name}"
        this.ciMetrics.timed measurementName, name, {
            sendMessageWithAudit(beforeRunMsg)
            body()
            sendMessageWithAudit(afterRunMsg)
        }
    } catch(e) {
        sendMessageWithAudit(failedRunMsg)
        throw e
    } finally {
        print "end of pipeline step: ${name}"
    }
}
