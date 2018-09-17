package org.centos.contra.jobdsl


class Triggers {

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