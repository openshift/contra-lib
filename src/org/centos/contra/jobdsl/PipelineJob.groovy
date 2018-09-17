package org.centos.contra.jobdsl


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
    void logRotate(def numKeep = 5, def artifactToKeep = 5) {
        job.with {
            logRotator {
                numToKeep(numKeep)
                artifactNumToKeep(artifactToKeep)
            }
        }
    }

    /**
     * Pass in a job trigger from the Triggers class
     * @param jobTrigger
     */
    void trigger(def jobTrigger) {
        job.with {
            triggers {
                jobTrigger
            }
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
}
