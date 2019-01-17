import org.centos.contra.pipeline.Utils

/**
 * Defines the header of a message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusHeader.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['topic'] = parameters['topic'] ?: "org.fedoraproject.prod.ci.koji-build.test.complete"

        parameters = utils.mapMerge([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating the message header failed!")
        }

        // sendCIMessage expects String arguments
        return utils.getMapString(mergedMessage)
    }
}
