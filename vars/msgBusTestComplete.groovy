import org.centos.contra.pipeline.Utils

/**
 * Defines the test.complete message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusTestComplete.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['contact'] = parameters['contact'] ?: msgBusContactContent()()
        parameters['run'] = parameters['run'] ?: msgBusRunContent()()
        parameters['artifact'] = parameters['artifact'] ?: msgBusArtifactContent()()
        parameters['pipeline'] = parameters['pipeline'] ?: msgBusPipelineContent()()
        parameters['system'] = parameters['system'] ?: msgBusSystemContent()()
        parameters['generated_at'] = parameters['generated_at'] ?: java.time.Instant.now().toString()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating the test complete message failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
