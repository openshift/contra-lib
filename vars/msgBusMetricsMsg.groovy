import org.centos.contra.pipeline.Utils

/**
 * Defines the metrics message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusMetricsMsg.json')

    return { Map runtimeArgs = [:] ->
        parameters['externalCall'] = parameters['externalCall'] ?: msgBusMetricsExternalCallContent()()
        parameters['generated_at'] = parameters['generated_at'] ?: java.time.Instant.now().toString()
        parameters['pipeline'] = parameters['pipeline'] ?: msgBusMetricsPipelineContent()()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating the metrics message failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
