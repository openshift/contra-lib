package org.centos.contra.jobdsl

import org.centos.contra.jobdsl.Utils


class PipelineJob {

    def job

    PipelineJob(def job, String name) {
        this.job = job.pipelineJob(name)
    }

    void logRotator(def days = 14) {
        job.with {
            definition {
                logRotator {
                    numToKeep(days)
                }
            }
        }
    }

    void ciEvent(Map checks) {
        job.with {
            configure Utils.ciEvent(checks)
        }
    }

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

    void gitHubTrigger() {
        job.with {
            triggers {
                githubPush()
            }
        }
    }

}