import org.centos.contra.pipeline.Utils

/**
 * Defines the Decision Content of a message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusDecisionContent.json')

    return { Map runtimeArgs = [:] ->
        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            def mergedMessage = utils.mergeBusMessage(parameters, defaults)
            return utils.getMapStringColon(mergedMessage)
        } catch(e) {
            throw new Exception("Creating message for Decision array failed: " + e)
        }
    }


}
