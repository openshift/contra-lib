import org.contralib.Utils


def call(Map parameters = [:]) {

    def message = parameters.get('message', '{}')

    def utils = new Utils()

    // Parse the message into a Map
    def ci_data = readJSON text: message.replace("\n", "\\n")

    def parsedMsg = [:]

    parsedMsg['repo'] = utils.repoFromRequest(ci_data['info']['request'][0])
    def branch = utils.setBuildBranch(ci_data['info']['request'][1])
    parsedMsg['branch'] = branch[0]
    parsedMsg['repo_branch'] = branch[1]

    return parsedMsg
}
