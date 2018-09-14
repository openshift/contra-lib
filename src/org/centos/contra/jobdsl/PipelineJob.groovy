package org.centos.contra.jobdsl

import org.centos.contra.jobdsl.Utils


class PipelineJob {

    def job

    PipelineJob(def job, String name) {
        this.job = job.pipelineJob(name)
    }

    void ciEvent(Map fields) {
        job.with {
            configure Utils.ciEvent(fields)
        }
    }

    void addGit(String repoUrl) {
        job.with {
            definition {
                cpsScm {
                    scm {
                        git(repoUrl)
                    }
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