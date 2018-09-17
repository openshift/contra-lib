package org.centos.contra.jobdsl


class Utils {

    /**
     * ciEvent trigger
     * @param checks
     * @return
     */
    static def ciEvent(Map msgChecks, String msgTopic, String fedMsgName = 'fedora-fedmsg') {
        return {
            it / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty' / 'triggers' << {
                'com.redhat.jenkins.plugins.ci.CIBuildTrigger' {
                    'spec'()
                    noSquash(false)
                    providerData(class: 'com.redhat.jenkins.plugins.ci.provider.data.FedMsgSubscriberProviderData') {
                        name(fedMsgName)
                        'overrides' {
                            topic(msgTopic)
                        }
                        'checks' {
                            'com.redhat.jenkins.plugins.ci.messaging.checks.MsgCheck' {
                                msgChecks.each { key, value ->
                                    field(key)
                                    expectedValue(value)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}