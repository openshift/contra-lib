/**
 * msgBus vars in use:
 * topicPrefix: The prefix of the message topic. Choices include org.fedoraproject.prod.ci and VirtualTopic.eng.ci
 * effortName: Name of the effort or pipeline
 * teamName
 * teamEmail
 * pipelineId: UUID for this pipeline run
 * teamIRC: team's irc channel
 * pipelineName: For use if the pipeline name differs from the effort name
 *
 * Example Usage:
 *
 *  setMsgEnvVars(stageName: 'run-stage') {
 *      runCode()
 *  }
 *
 * @param parameters
 * var: value which still set env.var = value
 * @return
 */

def call(Map parameters = [:]) {
    parameters.each { key, value ->
        env."${key}" = parameters.get(key)
    }
}
