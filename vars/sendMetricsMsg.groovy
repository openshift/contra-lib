import org.centos.contra.pipeline.Utils

/**
 * requires: env.topicPrefix
 * optional: env.msgProperties: This optional variable can be defined to pass along custom message headers in the messages sent by sendPipelineStatusMsg
 * Example Usage:
 * msgProperties = ['service': {}, 'retryData': {}, ...]
 * sendMetricsMsg(messageProperties) {
 * }
 *
 * @param metricsMap: A map containing the metrics to be sent over the bus
 * @return
 */

def call(Map metricsMap) {
    string b = ""
    // Make sure required env variables are set. The ones used in the
    // message bodies are enforced by the json closures.
    // Note: Error message should be changed if variables are added here
    if (!env.topicPrefix) {
        error("Missing env.topicPrefix required variable to use sendMetricsMsg")
    }

    def msgTopic = env.topicPrefix + ".pipeline.metrics"

    try {
        def params = []
        if (metricsMap['service']['params']) {
            metricsMap['service']['params'].each {
                b = "$it".replaceAll('"','')
                b = "$b".replaceAll("\n","")
                params.add("\"$b\"")
            }
            metricsMap['service']['params'] = params
        }
        service = msgBusMetricsServiceContent(
                metricsMap['service']
        )

        def retryData
        def iterations = []
        metricsMap['retryData']['iterations'].each {
            iterations.add(
                    msgBusMetricsRetryDataIterationContent(it)()
            )
        }
        if (metricsMap['retryData']['configuration']) {
            retryDataConfiguration = msgBusMetricsRetryDataConfigurationContent(
                    metricsMap['retryData']['configuration']
            )
            retryData = msgBusMetricsRetryDataContent(configuration: retryDataConfiguration(), iterations: iterations)
        } else {
            retryData = msgBusMetricsRetryDataContent(iterations: iterations)
        }

        externalCall = msgBusMetricsExternalCallContent(
                service: service(),
                source: metricsMap['source'],
                success: metricsMap['success'],
                start: metricsMap['start'],
                end: metricsMap['end'],
                retryData: retryData()
        )
        pipeline = env.productId ? msgBusMetricsPipelineContent(
                id: env.pipelineId,
                name: env.pipelineName,
                jenkinsUrl: env.JENKINS_URL,
                productId: env.productId
        ) : msgBusMetricsPipelineContent(
                id: env.pipelineId,
                name: env.pipelineName,
                jenkinsUrl: env.JENKINS_URL
        )
        metricsMsg = msgBusMetricsMsg(externalCall: externalCall(), pipeline: pipeline())

        // Send message
        def utils = new Utils()
        utils.sendMessage(msgTopic, env.msgProperties ?: "", metricsMsg())

    } catch(e) {
        println("No message was sent out on topic " + msgTopic + ". The error encountered was: " + e)
    }
}
