import org.centos.contra.pipeline.Utils

/**
 * Defines the CI Content of a message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusCIContent.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['url'] = parameters['url'] ?: env.JENKINS_URL

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        mergedMessage = utils.mergeBusMessage(parameters, defaults)

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }


}
