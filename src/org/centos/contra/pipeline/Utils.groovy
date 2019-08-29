package org.centos.contra.pipeline
import com.redhat.jenkins.plugins.ci.messaging.data.SendResult

/**
 * @param request - the url that refers to the package
 * @param prefix - env prefix
 * @return
 */
def repoFromRequest(String request) {

    def repo = null
    try {
        def gitMatcher = request =~ /git.+?\/([a-z0-9A-Z_\-\+\.]+?)(?:\.git|\?|#).*/
        def buildMatcher = request =~ /(?:koji-shadow|cli-build).+?\/([a-zA-Z0-9\-_\+\.]+)-.*/
        def pkgMatcher = request =~ /^([a-zA-Z0-9\-_\+\.]+$)/
        def srpmMatcher = request =~ /.+?\/([a-zA-Z0-9\.\-_\+]+)-[0-9a-zA-Z\.\_]+-[0-9\.].+.src.rpm/


        if (gitMatcher.matches()) {
            repo = gitMatcher[0][1]
        } else if (srpmMatcher.matches()) {
            repo = srpmMatcher[0][1]
        } else if (buildMatcher.matches()) {
            repo = buildMatcher[0][1]
        } else if (pkgMatcher.matches()) {
            repo = pkgMatcher[0][1]
        } else {
            throw new Exception("Invalid request url: ${request}")
        }
    } catch(e) {
        throw e
    }

    return repo
}

/**
 * Wrapper to parse json before injecting env variables
 * @param prefix
 * @param message
 * @return
 */
def flattenJSON(String message) {
    def ciMessage = readJSON text: message.replace("\n", "\\n")
    def ci_data = [:]
    return injectCIMessage(ci_data, ciMessage)
}

/**
 * Traverse a CI_MESSAGE with nested keys.
 * @param ci_data
 * @param ciMessage
 * @return env map with all keys at top level
 */
def injectCIMessage(def ci_data, def ciMessage) {

    ciMessage.each { key, value ->
        def new_key = key.replaceAll('-', '_')
        // readJSON uses JSON* and slurper uses LazyMap and ArrayList
        if (value instanceof groovy.json.internal.LazyMap || value instanceof net.sf.json.JSONObject) {
            injectCIMessage(ci_data, value)
        } else if (value instanceof java.util.ArrayList || value instanceof net.sf.json.JSONArray) {
            // value was an array itself
            injectArray(ci_data, value)
        } else {
            ci_data[new_key] =
                    value.toString().split('\n')[0].replaceAll('"', '\'')
        }
    }

    return ci_data
}

/**
 * Inject array values
 * @param ci_data
 * @param message
 * @return
 */
def injectArray(def ci_data, def message) {
    message.eachWithIndex { value, index ->
        ci_data[value] =
            value.toString().split('\n')[0].replaceAll('"', '\'')
    }
}

/**
 * Set branch and repo_branch based on the candidate branch
 * This is meant to be run with a CI_MESSAGE from a build task
 * @param tag - The tag from the request field e.g. f27-candidate
 * @return
 */
def setBuildBranch(String tag) {
    def branch = null
    def repo_branch = null

    try {
        if (tag.toLowerCase() == 'rawhide') {
            branch = tag
            repo_branch = 'master'
        } else {
            // assume that tag is branch-candidate
            tokentag = tag.tokenize('-')
            repo_branch = tokentag[0..tokentag.size()-2].join('-')
            branch = repo_branch
        }
    } catch(e) {
        throw new Exception('Something went wrong parsing branch', e)
    }

    return [branch, repo_branch]
}

/**
 * Initialize message audit file
 * @param auditFile audit file for messages
 * @return
 */
def initializeAuditFile(String auditFile) {
    // Ensure auditFile is available
    sh script: "rm -f ${auditFile}", label: "Deleting old files"
    String msgAuditFileDir = sh(script: "dirname ${auditFile}", label: "Getting dirname of the ${auditFile}", returnStdout: true).trim()
    sh script: "mkdir -p ${msgAuditFileDir}", label: "Creating directory: ${msgAuditFileDir}"
    sh script: "touch ${auditFile}", label: "Creating file: ${auditFile}"
    sh script: "echo '{}' >> ${auditFile}", label: "Adding '{}' to the ${auditFile}"
}

/**
 * Test if $tag tests exist for $mypackage on $mybranch in fedora dist-git
 * For mybranch, use fXX or master and pr_id is PR number (digits only)
 * @param mypackage
 * @param mybranch - Fedora branch
 * @param tag
 * @param pr_id    - PR number
 * @param namespace - rpms (default) or container
 * @return
 */
def checkTests(String mypackage, String mybranch, String tag, String pr_id=null, String namespace='rpms') {
    echo "Currently checking if package tests exist"
    sh script: "rm -rf ${mypackage}", label: "Deleting old packages"
    def repo_url = "https://src.fedoraproject.org/${namespace}/${mypackage}/"
    retry(5) {
        sh script: "git clone -b ${mybranch} --single-branch --depth 1 ${repo_url}",
        label: "Cloning ${repo_url} into the ${mybranch} branch"
    }
    if (pr_id != null) {
        dir("${mypackage}") {
            sh script: "git fetch -fu origin refs/pull/${pr_id}/head:pr", label: "Fetching ${pr_id} commit"
            // If fail to apply patch do not exit with error, but instead ignore the patch
            // this should avoid the pipeline to exit here without sending any topic to fedmsg
            try {
                // Setting git config and merge message in case we try to merge a closed PR
                sh script: "git -c 'user.name=Fedora CI' -c 'user.email=ci@lists.fedoraproject.org'  merge pr -m 'Fedora CI pipeline'",
                label: "Applying patch from pull request"
            } catch (err) {
                echo "FAIL to apply patch from PR, ignoring it..."
            }
        }
    }
    // if STR is installed use it to check for tags as it is more reliable
    if (sh(returnStatus: true, script: """rpm -q standard-test-roles""", label: "Checking if standard-test-roles are installed") == 0) {
        if (namespace != "tests") {
            return sh (returnStatus: true, script: "ansible-playbook --list-tags ${mypackage}/tests/tests*.yml | grep -e \"TASK TAGS: \\[.*\\<${tag}\\>.*\\]\"", label: "Getting list of tags") == 0
        } else {
            return sh (returnStatus: true, script: "ansible-playbook --list-tags ${mypackage}/tests*.yml | grep -e \"TASK TAGS: \\[.*\\<${tag}\\>.*\\]\"", label: "Getting list of tags") == 0
        }
    } else {
        if (namespace != "tests") {
            return sh (returnStatus: true, script: """grep -r '\\- '${tag}'\$' ${mypackage}/tests""", label: "Getting list of tags") == 0
        } else {
            return sh (returnStatus: true, script: """grep -r '\\- '${tag}'\$' ${mypackage}""", label: "Getting list of tags") == 0
        }
    }
}

/**
 * Library to parse Pagure PR CI_MESSAGE and check if
 * it is for a new commit added, the comment contains
 * some keyword, or if the PR was rebased
 * If notification = true, commit was added or it was rebased
 * @param message - The CI_MESSAGE
 * @param keyword - The keyword we care about
 * @return bool
 */
def checkUpdatedPR(def ci_data, String keyword) {

    if (ci_data['pullrequest']['comments']) {
        // Check if this comment is a merge notification
        if (ci_data['pullrequest']['status'] == 'Merged') {
            return false
        }
        if (ci_data['pullrequest']['comments'].last()['notification'] || ci_data['pullrequest']['comments'].last()['comment'].contains(keyword)) {
            return true
        } else {
            return false
        }
    }
    return true
}

/**
 *
 * @param openshiftProject name of openshift namespace/project.
 * @param nodeName podName we are going to verify.
 * @return
 */
def verifyPod(String openshiftProject, String nodeName=env.NODE_NAME) {
    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            def describeStr = openshift.selector("pods", nodeName).describe()
            out = describeStr.out.trim()

            sh script: 'mkdir -p podInfo', label: "Creating 'podInfo' directory"

            writeFile file: 'podInfo/node-pod-description-' + nodeName + '.txt',
                    text: out
            archiveArtifacts 'podInfo/node-pod-description-' + nodeName + '.txt'

            timeout(60) {
                echo "Ensuring all containers are running in pod: ${nodeName}"
                echo "Container names in pod ${nodeName}: "
                names       = openshift.raw("get", "pod",  "${nodeName}", '-o=jsonpath="{.status.containerStatuses[*].name}"')
                containerNames = names.out.trim()
                echo containerNames

                waitUntil {
                    def readyStates = openshift.raw("get", "pod",  "${nodeName}", '-o=jsonpath="{.status.containerStatuses[*].ready}"')

                    echo "Container statuses: "
                    echo containerNames
                    echo readyStates.out.trim().toUpperCase()
                    def anyNotReady = readyStates.out.trim().contains("false")
                    if (anyNotReady) {
                        echo "One or more containers not ready...see above message ^^"
                        return false
                    } else {
                        echo "All containers ready!"
                        return true
                    }
                }
            }
        }
    }
}

/**
 *
 * @param openshiftProject name of openshift namespace/project.
 * @param nodeName podName we are going to get container logs from.
 * @return
 */
def getContainerLogsFromPod(String openshiftProject, String nodeName=env.NODE_NAME) {
    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            sh script: 'mkdir -p podInfo', label: "Creating 'podInfo' directory"
            names       = openshift.raw("get", "pod",  "${nodeName}", '-o=jsonpath="{.status.containerStatuses[*].name}"')
            String containerNames = names.out.trim()

            containerNames.split().each {
                String log = containerLog name: it, returnLog: true
                writeFile file: "podInfo/containerLog-${it}-${nodeName}.txt",
                            text: log
            }
            archiveArtifacts "podInfo/containerLog-*.txt"
        }
    }
}

/**
 * Build image in openshift
 * @param openshiftProject Openshift Project
 * @param buildConfig
 * @return
 */
def buildImage(String openshiftProject, String buildConfig) {
    // - build in Openshift
    // - startBuild with a commit
    // - Get result Build and get imagestream manifest
    // - Use that to create a unique tag
    // - This tag will then be passed as an image input
    //   to the podTemplate/containerTemplate to create
    //   our slave pod.
    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            def result = openshift.startBuild(buildConfig,
                    "--commit",
                    "refs/pull/" + env.ghprbPullId + "/head",
                    "--wait")
            def out = result.out.trim()
            echo "Resulting Build: " + out

            def describeStr = openshift.selector(out).describe()
            out = describeStr.out.trim()

            def imageHash = sh(
                    script: "echo \"${out}\" | grep 'Image Digest:' | cut -f2- -d:",
                    label: "Getting Image Hash",
                    returnStdout: true
            ).trim()
            echo "imageHash: ${imageHash}"

            echo "Creating CI tag for ${openshiftProject}/${buildConfig}: ${buildConfig}:PR-${env.ghprbPullId}"

            openshift.tag("${openshiftProject}/${buildConfig}@${imageHash}",
                    "${openshiftProject}/${buildConfig}:PR-${env.ghprbPullId}")

            return "PR-" + env.ghprbPullId
        }
    }
}

def getCredentialsById(String credsId, String credsType = 'any') {
    def credClasses = [ // ordered by class name
                        sshKey    : com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.class,
                        cert      : com.cloudbees.plugins.credentials.common.CertificateCredentials.class,
                        password  : com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials.class,
                        any       : com.cloudbees.plugins.credentials.impl.BaseStandardCredentials.class,
                        dockerCert: org.jenkinsci.plugins.docker.commons.credentials.DockerServerCredentials.class,
                        file      : org.jenkinsci.plugins.plaincredentials.FileCredentials.class,
                        string    : org.jenkinsci.plugins.plaincredentials.StringCredentials.class,
    ]
    return com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
            credClasses[credsType],
            jenkins.model.Jenkins.instance
    ).findAll { cred -> cred.id == credsId }[0]
}

/**
 * Library to send message
 * @param msgProps - The message properties in key=value form, one key/value per line ending in '\n'
 * @param msgContent - Message content.
 * @param provider - Provider to send message on. If not passed, will default to env.MSG_PROVIDER
 * @return
 */
def sendMessage(String msgTopic, String msgProps, String msgContent, def provider=null) {

    msg_provider = provider ?: env.MSG_PROVIDER

    retry(10) {
        try {
            // 1 minute should be more than enough time to send the topic msg
            timeout(1) {
                try {
                    // Send message and return SendResult
                    sendResult = sendCIMessage messageContent: msgContent,
                            messageProperties: msgProps,
                            messageType: 'Custom',
                            overrides: [topic: msgTopic],
                            failOnError: true,
                            providerName: msg_provider
                    return sendResult
                } catch(e) {
                    throw e
                }
            }
        } catch(e) {
            echo "FAIL: Could not send message to ${msg_provider} on topic ${msgTopic}"
            echo e.getMessage()
            sleep 30
            error e.getMessage()
        }
    }
}

/**
 * Check data grepper for presence of a message
 * @param messageID message ID to track.
 * @param retryCount number of times to keep trying.
 * @param dataGrepperWebAddr - The url to the datagrepper instance. If not passed, it will default to env.dataGrepperUrl
 * @return
 */
def trackMessage(String messageID, int retryCount, def dataGrepperWebAddr=null) {
    dGWebAddress = dataGrepperWebAddr ?: env.dataGrepperUrl

    retry(retryCount) {
        echo "Checking datagrapper for presence of message..."
        def STATUSCODE = sh (returnStdout: true, script: """
            curl --insecure --silent --output /dev/null --write-out "%{http_code}" \'${dGWebAddress}/id?id=${messageID}&chrome=false&is_raw=false\'
        """, label: "Checking datagrapper for presence of message").trim()
        // We only want to wait if there are 404 errors
        echo "${STATUSCODE}"
        if (STATUSCODE.equals("404")) {
            error("message not found on datagrepper...")
        }
        if (STATUSCODE.startsWith("5")) {
            echo("WARNING: internal datagrepper server error...")
        } else {
            echo "found!"
        }
    }
}

/**
 * Allow to check custom service for presence of a message
 *
 * It is possible to use method pointer operator (.&) from groovy
 * if you do not want to create new closure.
 * http://docs.groovy-lang.org/latest/html/documentation/core-operators.html#method-pointer-operator
 *
 * @param trackClosure closure for checking successful delivery of message.
                       You need to throw exception (or to call error())
                       in case of failure. Closure need to accept one parameter com.redhat.jenkins.plugins.ci.messaging.data.SendResult
                       which is returned from the step sendCIMessage.
 * @param retryCount number of times to keep trying.
 * @return
 */
def trackMessageWithClosure(Closure trackClosure, int retryCount, SendResult sendResult) {
    retry(retryCount) {
        trackClosure(sendResult)
    }
}

/**
 *
 * @param a list of Maps to merge
 * @return
 */
def mapMerge(def sources) {
    if (sources.size() == 0) return [:]
    if (sources.size() == 1) return sources[0]

    sources.inject([:]) { result, source ->
        source.each { k, v ->
            result[k] = result[k] instanceof Map ? merge(result[k], v) : v
        }
        result
    }
}

/**
 *
 * @param a list of Maps to merge
 * @return
 */
def mapMergeQuotes(def sources) {
    if (sources.size() == 0) return [:]
    if (sources.size() == 1) return sources[0]

    sources.inject([:]) { result, source ->
        source.each { k, v ->
            if (v instanceof String && (v == "" || !(v[0] in ["\"", "{"]))) {
                v2 = "\"" + v + "\""
            } else {
                v2 = v
            }
            result[k] = result[k] instanceof Map ? merge(result[k], v2) : v2
        }
        result
    }
}

/**
 * Merge two messages. Used to merge the default message with a user supplied message.
 * @param content
 * @param defaults
 * @return
 */
def mergeBusMessage(Map content, Map defaults) {

    def mergedContent = [:]

    // merge in defaults value only
    defaults.each { k, v ->
        if (v['required']) {
            mergedContent[k] = v['value']
        }
    }
    // merge in user supplied content
    mergedContent << content

     // Now check if merged content contains required values and types
    mergedContent.each { k, v ->
        if (!validateBusKeyValue(k, v, defaults)) {
            throw new GroovyRuntimeException()
        }
    }

    return mergedContent
}

/**
 * Return a string of k:v pairs for a Map
 * @param Map myMap
 * @return String
 */
def getMapStringColon(Map myMap) {
   myString = "{"
   myMap.each { k, v ->
       myString = myString + "\"" + k + "\":" + v + ","
   }
   return myString.substring(0, myString.length() - 1) + "}"
}

/**
 * Return a string of k=v pairs for a Map
 * @param Map myMap
 * @return String
 */
def getMapString(Map myMap) {
   myString = ""
   myMap.each { k, v ->
       myString = myString + k + "=" + v + "\n"
   }
   return myString
}

/**
 * Compare a message key with the Message Bus Spec.
 * @param key
 * @param value
 * @param defaults
 * @return
 */
def validateBusKeyValue(def key, def value, Map defaults) {

    def isValid = false

    if (!defaults.containsKey(key)) {
        print "Invalid key for ${key}, ${value}"
    } else if ((value == 'null') && (defaults[key]['required'])) {
        print "Required value missing for ${key}, ${value}"
    // if value's type != expected type from defaults
    } else if (!jenkinsIsAssignableFrom(value.getClass(), Class.forName(defaults[key]['type']))) {
        print "Invalid type used for ${key}, ${value}"
    } else {
        isValid = true
    }

    return isValid
}

/**
 * Find out if Class A is a subclass of B
 * This duplicates Class.isAssignableFrom, but
 * works on the classes provided by plugins that
 * groovy doesn't know about
 * @param A - Class
 * @param B - Class
 * @return bool
 */
def jenkinsIsAssignableFrom(Class A, Class B) {
    if (A.isAssignableFrom(B)) {
        return true
    } else if (A.getSuperclass() != null) {
        return jenkinsIsAssignableFrom(A.getSuperclass(), B)
    } else {
        return false
    }

}

/**
 * ResultsDB and Jenkins use different terminology for
 * results, so get the ResultsDB one from the Jenkins one
 * @return
 */
def getBuildStatus() {
   myResult = null
   switch (currentBuild.currentResult) {
       case 'SUCCESS':
           myResult = 'PASSED'
           break
       case 'UNSTABLE':
           myResult = 'NEEDS_INSPECTION'
           break
       case 'FAILURE':
           myResult = 'FAILED'
           break
   }
   return myResult.toLowerCase()
}

/**
 * Figures out current OpenShift namespace (project name). That is the project
 * the Jenkins is running in.
 *
 * The method assumes that the Jenkins instance has kubernetes plugin installed
 * and properly configured.
 */
def getOpenshiftNamespace() {
    return openshift.withCluster() {
        openshift.project()
    }
}

/**
 * Figures out the Docker registry URL which is supposed to host all the images
 * for current OpenShift project.
 *
 * The method assumes that all images in the current project are stored in the
 * internal Docker registry. This is not 100% bullet proof, but should be good
 * enough as starting point.
 */
def getOpenshiftDockerRegistryURL() {
    return openshift.withCluster() {
        def someImageUrl = openshift.raw("get imagestream -o=jsonpath='{.items[0].status.dockerImageRepository}'").out.toString()
        String[] urlParts = someImageUrl.split('/')

        // there should be three parts in the image url:
        // <docker-registry-url>/<namespace>/<image-name:tag>
        if (urlParts.length != 3) {
            throw new IllegalStateException(
                    "Can not determine Docker registry URL!" +
                            " Unexpected image URL: $someImageUrl" +
                            " - expecting the URL in the following format:" +
                            " '<docker-registry-url>/<namespace>/<image-name:tag>'.")
        }
        def registryUrl = urlParts[0]
        registryUrl
    }
}

/**
 * Library to parse Pagure PR CI_MESSAGE and check if
 * it is for a new commit added, the comment contains
 * some keyword, or if the PR was rebased
 * If notification = true, commit was added or it was rebased
 * @param message - The CI_MESSAGE
 * @param keyword - The keyword we care about
 * @return bool
 */
def checkUpdatedPR(String message, String keyword) {

    // Parse the message into a Map
    def ci_data = readJSON text: message.replace("\n", "\\n")

    if (ci_data['pullrequest']['status']) {
        if (ci_data['pullrequest']['status'] != 'Open') {
            return false
        }
    }
    if (ci_data['pullrequest']['comments']) {
        if (ci_data['pullrequest']['comments'].last()['notification'] || ci_data['pullrequest']['comments'].last()['comment'].contains(keyword)) {
            return true
        } else {
            return false
        }
    }
    // Default to return true because this is called for pr.new messages as well
    return true
}

/**
 * Using the currentBuild, get a string representation
 * of the changelog.
 * @return String of changelog
 */
@NonCPS
def getChangeLogFromCurrentBuild() {
    MAX_MSG_LEN = 100
    def changeString = ""

    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            changeString += " - ${truncated_msg} [${entry.author}]\n"
            def files = new ArrayList(entry.affectedFiles)
            for (int k = 0; k < files.size(); k++) {
                def file = files[k]
                changeString += "    | (${file.editType.name})  ${file.path}\n"
            }
        }
    }

    if (!changeString) {
        changeString = " - No new changes\n"
    }
    return changeString
}

/**
 *
 * @param nick nickname to connect to IRC with
 * @param channel channel to connect to
 * @param message message to send
 * @param ircServer optional IRC server defaults to irc.freenode.net:6697
 * @return
 */
def sendIRCNotification(String nick, String channel, String message, String ircServer="irc.freenode.net:6697") {
    sh script: """
        (
        echo NICK ${nick}
        echo USER ${nick} 8 * : ${nick}
        sleep 5
        echo "JOIN ${channel}"
        sleep 10
        echo "NOTICE ${channel} :${message}"
        echo QUIT
        ) | openssl s_client -connect ${ircServer}
    """, label: "Sending IRC notification. NICK: ${nick}, CHANNEL: ${channel}, IRC Server: ${ircServer}"
}
