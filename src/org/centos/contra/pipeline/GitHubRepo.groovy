package org.centos.contra.pipeline


import org.kohsuke.github.GitHub
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRelease


class GitHubRepo {

    String username
    String password
    String repo
    GitHub gitHub
    GHRepository ghRepository

    def connect() {
        def connection = null
        try {
            connection = GitHub.connectUsingPassword(username, password)
        } catch(e) {
            throw new Exception("unable to connect to github: ${e.toString()}")
        }

        return connection
    }

    def mergePR(def prNumber, String mergeMsg) {
        GHPullRequest ghPullRequest = gitHubRepo().getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)
    }

    def gitHubRepo() {
        if (!gitHub) {
            gitHub = connect()
        }

        if (!ghRepository) {
            ghRepository = gitHub.getRepository(repo)
        }

        return ghRepository
    }

    def createRelease(String tag, String releaseMsg) {
        GHRelease ghRelease = gitHubRepo().createRelease(tag)
                                .body(releaseMsg)
                                .create()

        return ghRelease
    }

    def getReleaseByTagName(String tag) {
        GHRelease ghRelease = gitHubRepo().getReleaseByTagName(tag)

        return ghRelease
    }

    def getLatestRelease() {
        GHRelease ghRelease = gitHubRepo().getLatestRelease()

        return ghRelease

    }
}
