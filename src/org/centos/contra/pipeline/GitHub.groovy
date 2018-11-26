package org.centos.contra.pipeline


class GitHub implements Serializable {

    Map envVars

    boolean isPR() {
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

    boolean isMR() {
        isPR()
    }

    String sourceBranch() {
        'master'
    }

    String targetBranch() {
        'master'
    }

    String sourceNamespace() {
        "openshift"
    }

    String targetNamespace() {
        "openshift"
    }
}

