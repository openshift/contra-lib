import org.centos.contra.pipeline.Utils

/**
 * Defines the [brew-koji]-build-group.build.complete message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBus-Common-Build-Complete.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['contact'] = parameters['contact'] ?: msgBusContactContent()()
        parameters['artifact'] = parameters['artifact'] ?: msgBusArtifactContent()()
        parameters['generated_at'] = parameters['generated_at'] ?: java.time.Instant.now().toString()

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            def mergedMessage = utils.mergeBusMessage(parameters, defaults)
            return utils.getMapStringColon(mergedMessage)
        } catch(e) {
            throw new Exception("Creating the build complete message failed: " + e)
        }
    }
}
