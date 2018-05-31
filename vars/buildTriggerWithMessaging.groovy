/**
 * Wrapper for the validation stage of a build trigger
 * Example Usage:
 *
 * def valid = buildTrigger {
 *     return runValidation(env.CI_MESSAGE)
 * }
 *
 * @param body
 * @return
 */
def call(Map parameters = [:], Closure body = {}) {
    def skippedMsg = parameters.get('skippedMsg')
    def queuedMsg = parameters.get('queuedMsg')
    def result = false

    handlePipelineStep {
        result = body()
    }

    if (result) {

        if (queuedMsg) {
            sendMessageWithAudit(queuedMsg)
        }

    } else {

        echo "CI_MESSAGE was invalid. Skipping..."
        currentBuild.description = "*Build Skipped*"

        if (skippedMsg) {
            sendMessageWithAudit(skippedMsg)
        }

    }

    return result
}
