/**
 * requires: following env vars
 * env.topicPrefix: The prefix of the message topic. Choices include org.fedoraproject.prod.ci and VirtualTopic.eng.ci
 * env.effortName: Name of the effort or pipeline
 * env.teamName
 * env.teamEmail
 * env.pipelineId: UUID for this pipeline run
 * optional:
 * env.teamIRC: team's irc channel
 * env.pipelineName: For use if the pipeline name differs from the effort name
 * env.MSG_PROVIDER: This is actually required by the sendMessage function that ciStage uses. It is the provider configured in Jenkins on which to send messages
 * env.dataGrepperUrl: This is required by the sendMessageWithAudit function that ciStage uses. It tells the function where to look to confirm the message was sent as expected
 *
 * Example Usage:
 *
 *  ciStage('run-stage') {
 *      runCode()
 *  }
 *
 * @param stageName: Name to give the stage
 * @param body
 * @return
 */

def call(String stageName, Closure body) {
    // Make sure required env variables are set. The ones used in the
    // message bodies are enforced by the json closures.
    // Note: Error message should be changed if variables are added here
    if (!env.topicPrefix) {
        error("Missing topicPrefix required variable to use ciStage")
    }
    def runningTopic = env.topicPrefix + ".pipeline.stage.running"
    def completeTopic = env.topicPrefix + ".pipeline.stage.complete"

    // Create ci and pipeline arrays to place in messages
    myCIArray = env.teamIRC ? msgBusCIContent(name: env.effortName, team: env.teamName, irc: env.teamIRC, email: env.teamEmail) : msgBusCIContent(name: env.effortName, team: env.teamName, email: env.teamEmail)
    myStageArray = msgBusStageContent(name: stageName)
    myPipelineArray = env.pipelineName ? msgBusPipelineContent(name: env.pipelineName, id: env.pipelineId, stage: myStageArray()) : msgBusPipelineContent(name: env.effortName, id: env.pipelineId, stage: myStageArray())

    // Create stage running message
    runningMsg = msgBusStageMsg(ci: myCIArray(), pipeline: myPipelineArray())
    // send running message
    sendMessageWithAudit(msgTopic: runningTopic, msgContent: runningMsg())

    // Get current time
    long startTimeMillis = System.currentTimeMillis()

    stage(stageName) {
        body()
    }

    // Get end time
    long endTimeMillis = System.currentTimeMillis()

    float runTimeSeconds = ((endTimeMillis - startTimeMillis) / 1000)
    // Recreate pipeline array with runtime in the stage
    myStageArray = msgBusStageContent(name: stageName, runtime: runTimeSeconds)
    myPipelineArray = env.pipelineName ? msgBusPipelineContent(name: env.pipelineName, id: env.pipelineId, stage: myStageArray()) : msgBusPipelineContent(name: env.teamName, id: env.pipelineId, stage: myStageArray())

    // Create stage complete message
    completeMsg = msgBusStageMsg(ci: myCIArray(), pipeline: myPipelineArray())
    // Send complete message
    sendMessageWithAudit(msgTopic: completeTopic, msgContent: completeMsg())
}
