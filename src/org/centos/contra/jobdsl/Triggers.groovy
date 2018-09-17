package org.centos.contra.jobdsl


class Triggers {

    /**
     * ci event - fedMsgSubscriber
     * @param msgTopic
     * @param msgName
     * @param msgChecks
     * @return
     */
    static def fedMsgTrigger(String msgTopic, String msgName, Map msgChecks) {
        return {
            triggers {
                ciBuildTrigger {
                    providerData {
                        FedMsgSubscriberProviderData {
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

    static def gitHubTrigger() {
        return {
            githubPush()
        }
    }

    static def gitHubPullRequestTrigger(List jobAdmins, String triggerComment) {
        return {
            admins(jobAdmins)
            useGitHubHooks()
            triggerPhrase(triggerComment)
            extensions {
                buildStatus {
                    completedStatus('SUCCESS', 'There were no errors...')
                    completedStatus('FAILURE', 'There were errors, please check the build...')
                    completedStatus('ERROR', 'There was an error in the infrastructure...')
                }
            }
        }
    }
}