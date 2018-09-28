package org.centos.contra.pipeline


import org.kohsuke.github.GitHub
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHPullRequest


class GitUtils {

    String username
    String password
    GitHub gitHub

    GitUtils(String username, String password) {
        this.username = username
        this.password = password
        this.gitHub = null
    }

    def connect() {
        try {
            gitHub = GitHub.connectUsingPassword(username, password)
        } catch(e) {
            // throw a generic exception to mask credentials
            throw new Exception("unable to connect to github")
        }

    }

    def mergePR(def repo, def prNumber, def mergeMsg) {
        if (!gitHub) {
            connect()
        }

        GHRepository ghRepository = gitHub.getRepository(repo)
        GHPullRequest ghPullRequest = ghRepository.getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)
    }
}
