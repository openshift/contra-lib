package org.centos.contra.pipeline

import org.centos.contra.pipeline.Utils

import org.kohsuke.github.GitHub
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHPullRequest


class GitUtils {

    String credentialsId
    GitHub gitHub

    GitUtils(String credentialsId) {
        this.credentialsId = credentialsId
        this.gitHub = null
    }

    def connect() {
        def utils = new Utils()

        def credentials = utils.getCredentialsById(this.credentialsId, 'password')

        try {
            gitHub = GitHub.connectUsingPassword(credentials.getUsername(), credentials.getPassword().getPlainText())
        } catch(e) {
            // throw a generic exception to mask credentials
            throw new Exception("unable to connect to github")
        }

    }

    def mergePR(def repo, def prNumber, def mergeMsg) {
        if (!gitHub) {
            connect()
        }
        
        GHRepository ghRepository = github.getRepository(repo)
        GHPullRequest ghPullRequest = ghRepository.getPullRequest(prNumber)
        ghPullRequest.merge(mergeMsg)
    }
}
