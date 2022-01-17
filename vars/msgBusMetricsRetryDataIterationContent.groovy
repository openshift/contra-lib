import org.centos.contra.pipeline.Utils

/**
 * Defines the configuration content of a retry closure in an external call metrics message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBus-Metrics-RetryDataIteration-Content.json')

    return { Map runtimeArgs = [:] ->
        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating the retry configuration closure for retry metrics message failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
