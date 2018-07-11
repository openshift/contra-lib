import org.contralib.Utils


def call(Map parameters = [:]) {

    def message = parameters.get('message', '{}')

    def utils = new Utils()

    def parsedMsg = utils.flattenJSON(message.replace("\n", "\\n"))

    parsedMsg['repo'] = utils.repoFromRequest(ci_data['request'][0])
    def branch = utils.setBuildBranch(ci_data['request'][1])
    parsedMsg['branch'] = branch[0]
    parsedMsg['repo_branch'] = branch[1]

    return parsedMsg
}
