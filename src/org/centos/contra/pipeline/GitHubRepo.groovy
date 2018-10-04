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
    GitHub gitHub
    GHRepository ghRepository

    @NonCPS
    def connect() {
        try {
            gitHub = GitHub.connectUsingPassword(username, password)
        } catch(e) {
            throw new Exception("unable to connect to github: ${e.toString()}")
        }

        return gitHub

    }

    @NonCPS
    def mergePRByNumber(def prNumber, String mergeMsg) {
        GHPullRequest ghPullRequest = gitRepo().getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)
    }

    @NonCPS
    def rebasePR(GHPullRequest ghPullRequest) {
        ghPullRequest.merge(null, null, GHPullRequest.MergeMethod.REBASE)
    }

    @NonCPS
    def gitRepo() {
        if (!gitHub) {
            connect()
        }
        /*
        if (!ghRepository) {
            ghRepository = gitHub.getRepository(repo)
        }
        */

        return connect().getRepository(repo)
    }

    @NonCPS
    def createRelease(String tag, String releaseMsg, String sha) {
        GHRelease ghRelease = gitRepo().createRelease(tag)
                                .body(releaseMsg)
                                .commitish(sha)
                                .create()

        return ghRelease
    }

    @NonCPS
    def getReleaseByTagName(String tag) {
        GHRelease ghRelease = gitRepo().getReleaseByTagName(tag)

        return ghRelease
    }

    @NonCPS
    def getLatestRelease() {
        GHRelease ghRelease = gitRepo().getLatestRelease()

        return ghRelease

    }

    @NonCPS
    def createPR(String title, String head, String base, String body) {
        return gitRepo().createPullRequest(title, head, base, body)

    }
}
