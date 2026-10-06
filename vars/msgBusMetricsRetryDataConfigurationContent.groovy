import org.centos.contra.pipeline.Utils

/**
 * Defines the configuration content of a retry closure in an external call metrics message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBus-Metrics-RetryDataConfiguration-Content.json')

    return { Map runtimeArgs = [:] ->
        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            def mergedMessage = utils.mergeBusMessage(parameters, defaults)
            return utils.getMapStringColon(mergedMessage)
        } catch(e) {
            throw new Exception("Creating the retry configuration closure for retry metrics message failed: " + e)
        }
    }
}
