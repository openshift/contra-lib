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
 * buildPrefix: String - A unique name to your Jenkins instance. It's mainly used to prefix influx data
 * packageName: String - If set, metrics will be recorded about the package.
 * errorMsg: fedErrorMsg - a fedErrorMsg to send on job failure
 * completeMsg: fedCompleteMsg - a fedCompleteMsg to send on job completion
 * decorateBuild: Closure - A closure that will be called to decorate the build. Defaults to build_number and build result
 * preBuild: Closure - A closure that runs any pre build actions.
 * postBuild: Closure -  A closure that will run any post build actions. This can be used to archive artifacts or cleanup
 *             after the build has run
 * timeoutValue: Integer - How long before the job should timeout. Defaults to 120 minutes.
 * sendMetrics: Boolean - send metrics to influx or not
 * @param body
 * @return
 */
import org.centos.contra.pipeline.ciMetrics


def call(Map parameters = [:], Closure body) {
    def buildPrefix = parameters.get('buildPrefix', env.OPENSHIFT_BUILD_NAMESPACE)
    def packageName = parameters.get('package_name')
    def errorMsg = parameters.get('errorMsg')
    def completeMsg = parameters.get('completeMsg')
    def decorateBuild = parameters.get('decorateBuild')
    def preBuild = parameters.get('preBuild')
    def postBuild = parameters.get('postBuild')
    def timeoutValue = parameters.get('timeout', 120)
    def sendMetrics = parameters.get('sendMetrics', true)

    def cimetrics = ciMetrics.metricsInstance
    cimetrics.prefix = buildPrefix

    if (env.topicPrefix) {
        try {
            def runningTopic = env.topicPrefix + ".pipeline.running"
            // Create ci and pipeline arrays to place in messages
            // no def for myCIArray for scoping reasons
            myCIArray = env.teamIRC ? msgBusCIContent(name: env.effortName, team: env.teamName, irc: env.teamIRC, email: env.teamEmail) : msgBusCIContent(name: env.effortName, team: env.teamName, email: env.teamEmail)
            def myPipelineArray = env.pipelineName ? msgBusPipelineContent(name: env.pipelineName, id: env.pipelineId) : msgBusPipelineContent(name: env.effortName, id: env.pipelineId)
            // Create message
            runningMsg = msgBusPipelineMsg(ci: myCIArray(), pipeline: myPipelineArray())
            // Send message
            sendMessageWithAudit(msgTopic: runningTopic, msgContent: runningMsg())
            // Get current time to use later for pipeline runtime
            startTimeMillis = System.currentTimeMillis()
        } catch(e) {
            println("No message was sent out on topic " + env.topicPrefix + ".pipeline.running. The error encountered was: " + e)
        }
    }

    timeout(time: timeoutValue, unit: 'MINUTES') {

        try {
            if (preBuild) {
                preBuild()
            }

            body()
            topicSuffix = "complete"
        } catch (e) {
            // Set build result
            currentBuild.result = "FAILURE"
            topicSuffix = "error"

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
                topicSuffix = "error"
            }

            currentBuild.result = currentBuild.result ?: 'SUCCESS'

            if (env.topicPrefix) {
                try {
                    def endTopic = env.topicPrefix + ".pipeline." + topicSuffix
                    // Get end time
                    long endTimeMillis = System.currentTimeMillis()
                    float runTimeSeconds = ((endTimeMillis - startTimeMillis) / 1000)
                    // Recreate pipeline array with runtime
                    myPipelineArray = env.pipelineName ? msgBusPipelineContent(name: env.pipelineName, id: env.pipelineId, runtime: runTimeSeconds) : msgBusPipelineContent(name: env.effortName, id: env.pipelineId, runtime: runTimeSeconds)
                    // Create message
                    endMsg = msgBusPipelineMsg(ci: myCIArray(), pipeline: myPipelineArray())
                    // Send message
                    sendMessageWithAudit(msgTopic: endTopic, msgContent: endMsg())
                } catch(e) {
                    println("No message was sent out on topic " + env.topicPrefix + ".pipeline." + topicSuffix + ". The error encountered was: " + e)
                }
            }

            if (sendMetrics) {
                if (!buildPrefix) {
                    throw new RuntimeException('Must supply buildPrefix')
                }

                pipelineMetrics(buildPrefix: buildPrefix, package_name: packageName)
            }

            if (completeMsg) {
                sendMessageWithAudit(completeMsg())
            }

            if (decorateBuild) {
                decorateBuild()
            }

        }
    }

}
