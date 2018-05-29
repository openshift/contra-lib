/**
 * Schedule a Jenkins job
 * Example Usage:
 *
 * scheduleBuild(buildName: 'job-to-schedule', params: ['CI_MESSAGE': env.CI_MESSAGE])
 * @param parameters
 * @return
 */
def call(Map parameters) {

    def triggerRetryCount = parameters.get('triggerRetryCount', 3)
    def buildName = parameters.get('buildName')
    def params = parameters.get('params', [])

    def buildParams = []
    params.each { name, value ->
        buildParams << string(name: name, value: value)
    }

    try {
        retry(triggerRetryCount) {
            handlePipelineStep {

                build job: buildName,
                        parameters: buildParams
                wait: false

            }
        }
    } catch(e) {
        currentBuild.description = "*TRIGGER FAILURE*"
        error "Error: Build could not be added to queue after ${triggerRetryCount} tries."
        throw e
    }
}
