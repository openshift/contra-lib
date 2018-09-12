package org.centos.contra.pipeline

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


        if (gitMatcher.matches()) {
            repo = gitMatcher[0][1]
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
 * @param prefix
 * @param message
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
    sh "rm -f ${auditFile}"
    String msgAuditFileDir = sh(script: "dirname ${auditFile}", returnStdout: true).trim()
    sh "mkdir -p ${msgAuditFileDir}"
    sh "touch ${auditFile}"
    sh "echo '{}' >> ${auditFile}"
}

/**
 * Test if $tag tests exist for $mypackage on $mybranch in fedora dist-git
 * For mybranch, use fXX or master, or PR number (digits only)
 * @param mypackage
 * @param mybranch - Fedora branch or PR number
 * @param tag
 * @return
 */
def checkTests(String mypackage, String mybranch, String tag) {
    echo "Currently checking if package tests exist"
    sh "rm -rf ${mypackage}"
    if (mybranch.isNumber()) {
        sh "git clone https://src.fedoraproject.org/rpms/${mypackage}"
        dir("${mypackage}") {
            sh "git fetch -fu origin refs/pull/${mybranch}/head:pr"
            sh "git checkout pr"
            return sh (returnStatus: true, script: """
            grep -r '\\- '${tag}'\$' tests""") == 0
        }
    } else {
        return sh (returnStatus: true, script: """
        git clone -b ${mybranch} --single-branch https://src.fedoraproject.org/rpms/${mypackage}/ && grep -r '\\- '${tag}'\$' ${mypackage}/tests""") == 0
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

            sh 'mkdir -p podInfo'

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
