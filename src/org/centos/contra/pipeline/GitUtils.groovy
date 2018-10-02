package org.centos.contra.pipeline


import org.kohsuke.github.GitHub
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRelease


class GitUtils {

    String username
    String password
    String repoName
    GitHub gitHub
    GHRepository ghRepository

    GitUtils(String username, String password, String repo) {
        this.username = username
        this.password = password
        this.repoName = repo
        this.gitHub = connect()
        this.ghRepository = this.gitHub.getRepository(this.repoName)
    }

    def connect() {
        def connection = null
        try {
            connection = GitHub.connectUsingPassword(username, password)
        } catch(e) {
            throw new Exception("unable to connect to github: ${e.toString()}")
        }

        return connection
    }

    def mergePR(def prNumber, def mergeMsg) {
        GHPullRequest ghPullRequest = ghRepository.getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)
    }

    def createRelease(String tag, String releaseMsg) {
        GHRelease ghRelease = ghRepository.createRelease(tag)
                                .body(releaseMsg)
                                .create()

        return ghRelease
    }

    def getReleaseByTagName(String tag) {
        GHRelease ghRelease = ghRepository.getReleaseByTagName(tag)

        return ghRelease
    }

    def getLatestRelease() {
        GHRelease ghRelease = ghRepository.getLatestRelease()

        return ghRelease

    }
}
