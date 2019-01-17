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
        parameters['result'] = parameters['result'] ?: utils.getBuildStatus()
        parameters['raw'] = parameters['raw'] ?: utils.getBuildStatus()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating message for test array failed!")
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
