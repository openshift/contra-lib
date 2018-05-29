/**
 * Wrapper step to print debugging information and record the time spent running the wrapped code
 * Example Usage:
 *
 * msgProperties = ['msgTopic': 'stageName', 'msgProps': 'stage=stage1', 'msgContent': '{"pipelineResult":"success"}']
 * stage('mystage') {
 *     handlePipelineStepWithMessaging(msgProperties) {
 *         runCode()
 *     }
 * }
 *
 * @param parameters
 * @param body
 * @return
 */


def call(Map parameters, Closure body) {
    def measurementName = parameters.get('measurement', env.JOB_NAME)
    def name = parameters.get('stageName', env.STAGE_NAME ?: env.JOB_NAME)
    def msgProperties = parameters.get('msg', ['msgTopic': null, 'msgProps': null, 'msgContent': null])

    try {
        print "running pipeline step: ${name}"
        this.ciMetrics.timed measurementName, name, {
            sendMessageWithAudit(msgProperties)
            body()
            sendMessageWithAudit(msgProperties)
        }
    } catch(e) {
        throw e
    } finally {
        print "end of pipeline step: ${name}"
    }
}
