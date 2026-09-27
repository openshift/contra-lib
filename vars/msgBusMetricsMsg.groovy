import org.centos.contra.pipeline.Utils

/**
 * Defines the metrics message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBus-Metrics-Msg.json')

    return { Map runtimeArgs = [:] ->
        parameters['externalCall'] = parameters['externalCall'] ?: msgBusMetricsExternalCallContent()()
        parameters['generated_at'] = parameters['generated_at'] ?: java.time.Instant.now().toString()
        parameters['pipeline'] = parameters['pipeline'] ?: msgBusMetricsPipelineContent()()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            def mergedMessage = utils.mergeBusMessage(parameters, defaults)
            return utils.getMapStringColon(mergedMessage)
        } catch(e) {
            throw new Exception("Creating the metrics message failed: " + e)
        }
    }
}
