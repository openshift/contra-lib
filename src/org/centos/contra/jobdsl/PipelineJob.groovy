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
     * trigger from github webhook
     */
    void gitHubTrigger() {
        job.with {
            githubPush()
        }
    }

    /**
     * ghprBuilder trigger on comment
     * @param jobAdmins
     * @param triggerComment
     */
    void gitHubPullRequestTrigger(List jobAdmins, String triggerComment) {
        job.with {
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

    void gitlabTrigger(Map parameters = [:]) {
        boolean build_on_merge_request_events = parameters.build_on_merge_request_events ?: false
        boolean build_on_push_events = parameters.build_on_push_events ?: false
        boolean enable_ci_skip = parameters.enableCiSkip ?: false
        boolean set_build_description = parameters.set_build_description ?: false
        String rebuild_open_merge_requests = parameters.rebuild_open_merge_requests ?: 'never'
        String include_branches = parameters.include_branches
        String exclude_branches = parameters.exclude_branches

        job.with {
            triggers {
                gitlabPush {
                    buildOnMergeRequestEvents(build_on_merge_request_events)
                    buildOnPushEvents(build_on_push_events)
                    enableCiSkip(enable_ci_skip)
                    setBuildDescription(set_build_description)
                    rebuildOpenMergeRequest(rebuild_open_merge_requests)
                    if (include_branches) {
                        includeBranches(include_branches)
                    }
                    if (exclude_branches) {
                        excludeBranches(exclude_branches)
                    }
                }
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
    void addGit(Map parameters = [:]) {
        String repo_url = parameters.repo_url
        String repo_branch = parameters.repo_branch ?: 'master'
        String lightweight_co = parameters.lightweight_co ?: false
        String compare_remote = parameters.compare_remote ?: 'origin'
        String compare_target = parameters.compare_target ?: 'master'

        job.with {
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {
                                url(repo_url)
                            }
                            branch(repo_branch)
                            extensions {
                                changelogToBranch {
                                    options {
                                        compareRemote(compare_remote)
                                        compareTarget(compare_target)
                                    }
                                }
                            }
                        }
                    }
                    lightweight(lightweight_co)
                }
            }
        }
    }
}
