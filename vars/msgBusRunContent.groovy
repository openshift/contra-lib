import org.centos.contra.pipeline.Utils

/**
 * Defines the run content of a message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusRunContent.json')

    return { Map runtimeArgs = [:] ->
        // Set defaults that can't go in json file
        parameters['url'] = parameters['url'] ?: JENKINS_URL + 'blue/organizations/jenkins/' + env.JOB_NAME + '/detail/' + env.JOB_NAME + '/' + env.BUILD_NUMBER + '/pipeline/'
        parameters['log'] = parameters['log'] ?: env.BUILD_URL + 'console'
        parameters['rebuild'] = parameters['rebuild'] ?: env.BUILD_URL + 'rebuild/parameterized'

        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        mergedMessage = utils.mergeBusMessage(parameters, defaults)

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }
}
