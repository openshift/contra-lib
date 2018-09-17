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
     * ci event - fedMsgSubscriber
     * @param msgTopic
     * @param msgName
     * @param msgChecks
     * @return
     */
    void fedMsgTrigger(String msgTopic, String msgName, Map msgChecks) {
        job.with {
            triggers {
                ciBuildTrigger {
                    providerData {
                        fedMsgSubscriberProviderData {
                            name(msgName)
                            overrides {
                                topic(msgTopic)
                            }
                            checks {
                                msgCheck {
                                    msgChecks.each { key, value ->
                                        field(key)
                                        expectedValue(value)
                                    }
                                }
                            }
                        }
                    }
                    noSquash(true)
                }
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
