import org.centos.contra.pipeline.Utils

/**
 * Defines the pipeline content of a message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusPipelineContent.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['name'] = parameters['name'] ?: env.JOB_NAME
        parameters['build'] = parameters['build'] ?: env.BUILD_NUMBER
        parameters['id'] = parameters['id'] ?: UUID.randomUUID().toString()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating message for pipeline array failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
