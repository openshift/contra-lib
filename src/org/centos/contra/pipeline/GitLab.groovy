package org.centos.contra.pipeline

/**
 * A class to parse GitLab environment variables
 */
class GitLab implements Serializable {
    Map envVars

    String targetBranch() {
        envVars.gitlabTargetBranch
    }

    String sourceBranch() {
        envVars.gitlabSourceBranch
    }

    String repoUrl() {
        envVars.gitlabSourceRepoURL
    }

    String branch() {
        envVars.gitlabBranch
    }

    String targetRepo() {
        envVars.gitlabTargetRepoName
    }

    String targetNamespace() {
        envVars.gitlabTargetNamespace
    }

    String sourceNamespace() {
        envVars.gitlabSourceNamespace
    }

    boolean isMR() {
        // request is from a fork
        if (sourceNamespace() != targetNamespace()) {
            return true
        }

        // request is from different local branch
        if (sourceBranch() != targetBranch()) {
            return true
        }

        // request is not a MR
        return false
    }

    boolean isPR() {
        isMR()
    }

    String sourceRepoUrl() {
        envVars.gitlabSourceRepoHttpUrl
    }
}
