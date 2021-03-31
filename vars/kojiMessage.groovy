import org.centos.contra.pipeline.Utils


def call(Map parameters = [:]) {

    def message = parameters.get('message', '{}')
    def ignoreErrors = parameters.get('ignoreErrors', false)

    def utils = new Utils()

    def parsedMsg = readJSON text: message.replace("\n", "\\n")

    try {
        if (parsedMsg['request']) {
            parsedMsg['repo'] = utils.repoFromRequest(parsedMsg['request'][0])
            def branch = utils.setBuildBranch(parsedMsg['request'][1])
            parsedMsg['branch'] = branch[0]
            parsedMsg['repo_branch'] = branch[1]
        }
    } catch (e) {
        if (ignoreErrors) {
            println("Ignoring error message: " + e)
        } else {
            throw e
        }
    }

    return parsedMsg
}
