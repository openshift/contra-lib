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

def call(Map parameters, Closure body) {
    def measurementName = parameters.get('measurement', env.JOB_NAME)
    def name = parameters.get('stageName', env.STAGE_NAME ?: env.JOB_NAME)

    try {
        print "running pipeline step: ${name}"
        this.ciMetrics.timed measurementName, name, {
            body()
        }
    } catch(e) {
        throw e
    } finally {
        print "end of pipeline step: ${name}"
    }
}
