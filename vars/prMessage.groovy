def call(Map parameters = [:]) {

    def message = parameters.get('message', '{}')
    def prefix = parameters.get('prefix', 'fed')

    // Parse the message into a Map
    def ci_data = readJSON text: message.replace("\n", "\\n")

    def parsedMsg = [:]

    // If we have a 'pullrequest' key in the CI_MESSAGE, for each key under 'pullrequest', we
    // * prepend the key name with prefix_
    // * replace any '-' with '_'
    // * truncate the value for the key at the first '\n' character
    // * replace any double-quote characters with single-quote characters in the value for the key.

    if (ci_data['pullrequest']) {
        ci_data.pullrequest.each { key, value ->
            parsedMsg."${prefix}_${key.toString().replaceAll('-', '_')}" =
                    value.toString().split('\n')[0].replaceAll('"', '\'')
        }
        if (parsedMsg."${prefix}_branch" == 'master'){
            parsedMsg.branch = 'rawhide'
        } else {
            parsedMsg.branch = parsedMsg."${prefix}_branch"
        }
        // To support existing workflows, create some env vars
        // that map to vars from commit CI_MESSAGEs
        // Get the repo name
        if (ci_data['pullrequest']['project']['name']) {
            parsedMsg."${prefix}_repo" = ci_data['pullrequest']['project']['name']
                    .toString().split('\n')[0]
                    .replaceAll('"', '\'')
        }
        // Get the namespace value
        if (ci_data['pullrequest']['project']['namespace']) {
            parsedMsg."${prefix}_namespace" = ci_data['pullrequest']['project']['namespace']
                    .toString().split('\n')[0]
                    .replaceAll('"', '\'')
        }
        // Get the username value
        if (ci_data['pullrequest']['user']['name']) {
            parsedMsg."${prefix}_username" = ci_data['pullrequest']['user']['name']
                    .toString().split('\n')[0]
                    .replaceAll('"', '\'')
        }
        // Create a bogus rev value to use in build descriptions
        if (parsedMsg."${prefix}_id") {
            parsedMsg."${prefix}_rev" = "PR-" + parsedMsg."${prefix}_id"
            parsedMsg."${prefix}_pr_id" = parsedMsg."${prefix}_id"
        }
        // Get the last comment id as it was requested
        if (ci_data['pullrequest']['comments']) {
            parsedMsg."${prefix}_lastcid" = ci_data['pullrequest']['comments'].last()['id']
        }
    }

    return parsedMsg

}
