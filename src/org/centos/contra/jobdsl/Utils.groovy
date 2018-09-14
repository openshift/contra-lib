package org.centos.contra.jobdsl


class Utils {

    /**
     * ciEvent trigger
     * @param checks
     * @return
     */
    static def ciEvent(Map checks) {
        return {
            it / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty' / 'triggers' << {
                'com.redhat.jenkins.plugins.ci.CIBuildTrigger' {
                    'spec'()
                    noSquash(false)
                    providerData(class: 'com.redhat.jenkins.plugins.ci.provider.data.FedMsgSubscriberProviderData') {
                        name('fedora-fedmsg')
                        'overrides' {
                            topic('org.fedoraproject.prod.buildsys.build.state.change')
                        }
                        'checks' {
                            'com.redhat.jenkins.plugins.ci.messaging.checks.MsgCheck' {
                                checks.each { key, value ->
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