package org.centos.contra.jobdsl


class MultiBranchJob {

    def job

    MultiBranchJob(def job, String name) {
        this.job = job.multibranchPipelineJob(name)
    }

    void addGitHub(String repoName, String owner, String credentials = null) {

        job.with {
            branchSources {
                github {
                    if (credentials) {
                        scanCredentialsId(credentials)
                    }
                    repoOwner(owner)
                    repository(repoName)
                }
            }
        }

    }

    void addComment(String comment) {
        job.with {
            configure commentTrigger(comment)
        }
    }

    void discardOldBranches(def days = 7) {
        job.with {
            orphanedItemStrategy {
                discardOldItems {
                    numToKeep(days)
                }
            }
        }
    }

    static def commentTrigger(String comment) {

        return {
            it / sources / 'data' / 'jenkins.branch.BranchSource' << {
                strategy(class: 'jenkins.branch.DefaultBranchPropertyStrategy') {
                    properties(class: 'java.util.Arrays$ArrayList') {
                        a(class: 'jenkins.branch.BranchProperty-array') {
                            if (comment == "\\[merge\\]") {
                                'jenkins.branch.NoTriggerBranchProperty'()

                            }
                            'com.adobe.jenkins.github__pr__comment__build.TriggerPRCommentBranchProperty' {
                                commentBody(comment)
                            }
                        }
                    }
                }
            }
        }
    }

    static def scriptPath(String pipelineScript) {
        return {
            it / factory(class: "org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory") << {
                scriptPath(pipelineScript)
            }
        }
    }
}

