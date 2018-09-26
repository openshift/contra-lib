package org.centos.contra.pipeline

import org.centos.contra.pipeline.Utils

import org.kohsuke.github.GitHub


class GitUtils {

    def connect() {
        def utils = new Utils()

        def credentials = utils.getCredentialsById('contra-sample-project-docker-credentials', 'password')

        GitHub gitHub = GitHub.connectUsingPassword(credentials.getUsername(), credentials.getPassword().getPlainText())

    }
}
