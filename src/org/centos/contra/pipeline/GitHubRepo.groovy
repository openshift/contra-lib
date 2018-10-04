package org.centos.contra.pipeline


import org.kohsuke.github.GitHub
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRelease

import com.cloudbees.groovy.cps.NonCPS


class GitHubRepo implements Serializable {

    String username
    String password
    String repo

    @NonCPS
    def connect() {
        def connection = null
        try {
            connection = GitHub.connectUsingPassword(username, password)
        } catch(e) {
            throw new Exception("unable to connect to github: ${e.toString()}")
        }

        return connection
    }

    @NonCPS
    def mergePRByNumber(def prNumber, String mergeMsg) {
        GHPullRequest ghPullRequest = gitRepo().getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)
    }

    def rebasePR(GHPullRequest ghPullRequest) {
        ghPullRequest.merge(null, null, GHPullRequest.MergeMethod.REBASE)
    }

    def getRepo() {
        return connect().getRepository(repo)
    }

    @NonCPS
    def createRelease(String tag, String releaseMsg, String sha) {
        return getRepo().createRelease(tag)
                .body(releaseMsg)
                .commitish(sha)
                .create()
    }

    @NonCPS
    def getReleaseByTagName(String tag) {
        return getRepo().getReleaseByTagName(tag)
    }

    @NonCPS
    def getLatestRelease() {
        return getRepo().getLatestRelease()
    }

    def createPR(String title, String head, String base, String body) {
        return getRepo().createPullRequest(title, head, base, body)

    }
}
