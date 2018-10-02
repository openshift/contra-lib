package org.centos.contra.pipeline


import org.kohsuke.github.GitHub
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRelease

import com.cloudbees.groovy.cps.NonCPS


class GitUtils {

    String username
    String password
    GitHub gitHub

    @NonCPS
    GitUtils(String username, String password) {
        this.username = username
        this.password = password
        this.gitHub = connect()
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

    def mergePR(def prNumber, String mergeMsg, String repo) {
        if (!gitHub) {
            connect()
        }
        GHRepository ghRepository = gitHub.getRepository(repo)
        GHPullRequest ghPullRequest = ghRepository.getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)
    }

    def createRelease(String tag, String releaseMsg, String repo) {
        if (!gitHub) {
            connect()
        }
        GHRepository ghRepository = gitHub.getRepository(repo)
        GHRelease ghRelease = ghRepository.createRelease(tag)
                                .body(releaseMsg)
                                .create()

        return ghRelease
    }

    def getReleaseByTagName(String tag, String repo) {
        if (!gitHub) {
            connect()
        }
        GHRepository ghRepository = gitHub.getRepository(repo)
        GHRelease ghRelease = ghRepository.getReleaseByTagName(tag)

        return ghRelease
    }

    def getLatestRelease(String repo) {
        if (!gitHub) {
            connect()
        }
        GHRepository ghRepository = gitHub.getRepository(repo)
        GHRelease ghRelease = ghRepository.getLatestRelease()

        return ghRelease

    }
}
