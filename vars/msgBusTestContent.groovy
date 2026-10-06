import org.centos.contra.pipeline.Utils

/**
 * Defines the test content of a message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusTestContent.json')

    return { Map runtimeArgs = [:] ->

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            def mergedMessage = utils.mergeBusMessage(parameters, defaults)
            return utils.getMapStringColon(mergedMessage)
        } catch(e) {
            throw new Exception("Creating message for test array failed: " + e)
        }
    }
}
