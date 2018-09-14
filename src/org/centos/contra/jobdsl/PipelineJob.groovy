package org.centos.contra.jobdsl

import org.centos.contra.jobdsl.Utils


class PipelineJob {

    def job

    PipelineJob(def job, String name) {
        this.job = job.pipelineJob(name)
    }

    /**
     * Build rotation
     * @param numKeep
     * @param artifactToKeep
     */
    void logRotator(def numKeep = 5, def artifactToKeep = 5) {
        job.with {
            logRotator {
                numToKeep(numKeep)
                artifactNumToKeep(artifactToKeep)
            }
        }
    }

    /**
     * Trigger on a ciEvent
     * @param msgChecks
     */
    void ciEvent(Map msgChecks) {
        job.with {
            configure Utils.ciEvent(msgChecks)
        }
    }

    /**
     * Add git repository
     * @param repo
     */
    void addGit(Map repo) {
        job.with {
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {
                                url(repo['repoUrl'])
                            }
                            branch(repo['branch'])
                        }
                    }
                    lightweight(true)
                }
            }
        }
    }

    /**
     * Trigger on github push
     */
    void gitHubTrigger() {
        job.with {
            triggers {
                githubPush()
            }
        }
    }

}