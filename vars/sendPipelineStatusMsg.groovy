/**
 * requires: env.topicPrefix
 * Example Usage:
 *
 * sendPipelineStatusMsg('complete') {
 * }
 *
 * @param topicSuffix: String - One of [queued,running,complete,error]
 * @return
 */

def call(String topicSuffix) {
    // Make sure required env variables are set. The ones used in the
    // message bodies are enforced by the json closures.
    // Note: Error message should be changed if variables are added here
    if (!env.topicPrefix) {
        error("Missing env.topicPrefix required variable to use sendPipelineStatusMsg")
    }

    try {
        def msgTopic = env.topicPrefix + ".pipeline." + topicSuffix
        def myCIArray = env.teamIRC ? msgBusCIContent(name: env.effortName, team: env.teamName, irc: env.teamIRC, email: env.teamEmail) : msgBusCIContent(name: env.effortName, team: env.teamName, email: env.teamEmail)
        // Get runtime for pipeline array
        float runTimeSeconds = (currentBuild.getDuration() / 1000)
        def myPipelineArray = env.pipelineName ? msgBusPipelineContent(name: env.pipelineName, id: env.pipelineId) : msgBusPipelineContent(name: env.effortName, id: env.pipelineId, runtime: runTimeSeconds)

        // Create message
        pipelineMsg = msgBusPipelineMsg(ci: myCIArray(), pipeline: myPipelineArray())
        // Send message
        sendMessageWithAudit(msgTopic: msgTopic, msgContent: pipelineMsg())

    } catch(e) {
        println("No message was sent out on topic " + env.topicPrefix + ".pipeline." + topicSuffix + ". The error encountered was: " + e)
    }
}
