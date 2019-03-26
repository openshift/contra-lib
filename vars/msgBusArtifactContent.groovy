import org.centos.contra.pipeline.Utils

/**
 * Defines the Artifact Content of a message
 * This will merge parameters with the defaults and will validate each parameter
 * @param parameters
 * @return HashMap
 */
def call(Map parameters = [:]) {

    def utils = new Utils()

    def defaults = readJSON text: libraryResource('msgBusArtifactContent.json')

    return { Map runtimeArgs = [:] ->
        if (!parameteres.containsKey('type')) {
            throw new Exception("Error: Did not pass a type in to the artifact closure")
        }
        // Perform checks for type specific required fields
        switch (parameters['type']) {
            case 'component-version':
                if (!parameters.containsKey('component') || !parameters.containsKey('version')) {
                    throw new Exception("Error: Missing required fields for component-version artifact")
                }
                break
            case 'container-image':
                if (!parameters.containsKey('repository') || !parameters.containsKey('digest') || !parameters.containsKey('issuer') || !parameters.containsKey('scratch') || !parameters.containsKey('id')) {
                    throw new Exception("Error: Missing required fields for container-image artifact")
                }
                break
            case 'product':
                if (!parameters.containsKey('id') || !parameters.containsKey('name') || !parameters.containsKey('version') || !parameters.containsKey('release') || !parameters.containsKey('architecture') || !parameters.containsKey('phase') || !parameters.containsKey('build') || !parameters.containsKey('state')) {
                    throw new Exception("Error: Missing required fields for product artifact")
                }
                parameters['nvr'] = parameters['nvr'] ?: parameters['name'] + '-' + parameters['version'] + '-' + parameters['release']
                break
            case 'productmd-compose':
                if (!parameters.containsKey('id') || !parameters.containsKey('compose_type')) {
                    throw new Exception("Error: Missing required fields for productmd-compose artifact")
                }
                break
            case 'pull-request':
                if (!parameters.containsKey('id') || !parameters.containsKey('comment_id') || !parameters.containsKey('commit_hash') || !parameters.containsKey('repository') || !parameters.containsKey('issuer')) {
                    throw new Exception("Error: Missing required fields for pull-request artifact")
                }
                break
            case 'redhat-container-group':
                if (!parameters.containsKey('id') || !parameters.containsKey('component') || !parameters.containsKey('errata_id') || !parameters.containsKey('images')) {
                    throw new Exception("Error: Missing required fields for redhat-container-group artifact")
                }
                break
            case 'redhat-container-image':
                if (!parameters.containsKey('id') || !parameters.containsKey('component') || !parameters.containsKey('full_name') || !parameters.containsKey('issuer') || !parameters.containsKey('nvr') || !parameters.containsKey('scratch')) {
                    throw new Exception("Error: Missing required fields for redhat-container-image artifact")
                }
                break
            case 'rpm-build-group':
                if (!parameters.containsKey('id') || !parameters.containsKey('builds') || !parameters.containsKey('repository')) {
                    throw new Exception("Error: Missing required fields for rpm-build-group artifact")
                }
                break
            case 'rpm-build':
                if (!parameters.containsKey('id') || !parameters.containsKey('issuer') || !parameters.containsKey('component') || !parameters.containsKey('nvr') || !parameters.containsKey('scratch')) {
                    throw new Exception("Error: Missing required fields for rpm-build artifact")
                }
                break
            default:
                throw new Exception("Error: Unsupported artifact type: " + parameters['type'])
                break
        }
        parameters = utils.mapMergeQuotes([parameters, runtimeArgs])
        try {
            mergedMessage = utils.mergeBusMessage(parameters, defaults)
        } catch(e) {
            throw new Exception("Creating message for artifact array failed: " + e)
        }

        // sendCIMessage expects String arguments
        return utils.getMapStringColon(mergedMessage)
    }


}
