package org.contralib

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
