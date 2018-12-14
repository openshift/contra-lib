import org.centos.contra.pipeline.Utils

/**
 * Defines the pipeline.stage messages
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusStageMsg.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['ci'] = parameters['ci'] ?: msgBusCIContent()()
        parameters['run'] = parameters['run'] ?: msgBusRunContent()()
        parameters['artifact'] = parameters['artifact'] ?: msgBusArtifactContent()()
        parameters['pipeline'] = parameters['pipeline'] ?: msgBusPipelineContent()()
        parameters['stage'] = parameters['stage'] ?: msgBusStageContent()()
        parameters['generated_at'] = parameters['generated_at'] ?: java.time.Instant.now().toString()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        mergedMessage = utils.mergeBusMessage(parameters, defaults)

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
