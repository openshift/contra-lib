package org.centos.jobdsl



class multiBranchJob {

    static def commentTrigger(String comment) {
        return {
            'org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject' / 'sources' / 'data' / 'jenkins.branch.BranchSource' << {
                strategy(class: 'jenkins.branch.DefaultBranchPropertyStrategy') {
                    properties(class: 'java.util.Arrays$ArrayList') {
                        a(class: 'jenkins.branch.BranchProperty-array') {
                            'com.adobe.jenkins.github__pr__comment__build.TriggerPRCommentBranchProperty' {
                                commentBody(comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

