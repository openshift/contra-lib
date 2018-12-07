package org.centos.contra.pipeline


import org.kohsuke.github.GitHub
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRelease


class GitHubAPI implements Serializable {

    String username
    String password
    String repo

    def connect() {
        def connection = null
        try {
            connection = GitHub.connectUsingPassword(username, password)
        } catch(e) {
            throw new Exception("unable to connect to github: ${e.toString()}")
        }

        return connection
    }

    def mergePRByNumber(def prNumber, String mergeMsg) {
        GHPullRequest ghPullRequest = getRepo().getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)

        return ghPullRequest.getMergeCommitSha()
    }

    def rebasePRByNumber(def prNumber) {
        GHPullRequest ghPullRequest = getRepo().getPullRequest(prNumber)
        ghPullRequest.merge(null, null, GHPullRequest.MergeMethod.REBASE)

        return ghPullRequest.getMergeCommitSha()
    }

    def rebasePR(GHPullRequest ghPullRequest) {
        ghPullRequest.merge(null, null, GHPullRequest.MergeMethod.REBASE)

        return ghPullRequest.getMergeCommitSha()
    }

    def getRepo() {
        return connect().getRepository(repo)
    }

    def createRelease(String tag, String releaseMsg, String sha) {
        return getRepo().createRelease(tag)
                .body(releaseMsg)
                .commitish(sha)
                .create()
    }

    def getReleaseByTagName(String tag) {
        return getRepo().getReleaseByTagName(tag)
    }

    def getLatestRelease() {
        return getRepo().getLatestRelease()
    }

    def createPR(String title, String head, String base, String body) {
        return getRepo().createPullRequest(title, head, base, body)

    }
}
