/**
 * requires: env.topicPrefix
 * optional: env.msgProperties: This optional variable can be defined to pass along custom message headers in the messages sent by sendPipelineStatusMsg
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
        myDocs = env.docsLink ?: env.JENKINS_URL
        def myContactArray = env.teamIRC ? msgBusContactContent(name: env.effortName, team: env.teamName, irc: env.teamIRC, email: env.teamEmail, docs: myDocs) : msgBusContactContent(name: env.effortName, team: env.teamName, email: env.teamEmail, docs: myDocs)
        if (topicSuffix in ['complete','error']) {
            // Get runtime for pipeline array
            float runTimeSeconds = (currentBuild.getDuration() / 1000)
            myPipelineArray = env.pipelineName ? msgBusPipelineContent(name: env.pipelineName, id: env.pipelineId, runtime: runTimeSeconds) : msgBusPipelineContent(name: env.effortName, id: env.pipelineId, runtime: runTimeSeconds)
        } else {
            myPipelineArray = env.pipelineName ? msgBusPipelineContent(name: env.pipelineName, id: env.pipelineId) : msgBusPipelineContent(name: env.effortName, id: env.pipelineId)
        }

        // Create message
        pipelineMsg = msgBusPipelineMsg(contact: myContactArray(), pipeline: myPipelineArray())
        // Send message
        sendMessageWithAudit(msgTopic: msgTopic, msgProps: env.msgProperties ?: "", msgContent: pipelineMsg())

    } catch(e) {
        println("No message was sent out on topic " + env.topicPrefix + ".pipeline." + topicSuffix + ". The error encountered was: " + e)
    }
}
