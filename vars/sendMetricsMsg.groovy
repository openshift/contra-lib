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
    // Make sure required env variables are set. The ones used in the
    // message bodies are enforced by the json closures.
    // Note: Error message should be changed if variables are added here
    if (!env.topicPrefix) {
        error("Missing env.topicPrefix required variable to use sendMetricsMsg")
    }

    def msgTopic = env.topicPrefix + ".pipeline.metrics"

    try {
        service = msgBusMetricsServiceContent(
                metricsMap['service']
        )
        error = metricsMap['error'] ? msgBusMetricsErrorContent(code: metricsMap['error']['code'], message: metricsMap['error']['message']) : null

        def retryData
        if (metricsMap['retryData']['configuration']) {
            retryDataConfiguration = msgBusMetricsRetryDataConfigurationContent(
                    metricsMap['retryData']['configuration']
            )
            retryData = msgBusMetricsRetryDataContent(configuration: retryDataConfiguration(), iterations: metricsMap['retryData']['iterations'])
        } else {
            retryData = msgBusMetricsRetryDataContent(iterations: metricsMap['retryData']['iterations'])
        }

        externalCall = metricsMap['error'] ? msgBusMetricsExternalCallContent(
                service: service(),
                source: metricsMap['source'],
                success: metricsMap['success'],
                error: error(),
                start: metricsMap['start'],
                end: metricsMap['end'],
                retryData: retryData()
        ) : msgBusMetricsExternalCallContent(
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
