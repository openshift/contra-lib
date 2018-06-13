# A general library for upstream ci

#### Example Usage:
```
stageVars = [repo: 'vim']
msgHeader = fedMsgHeader(branch: 'fed_repo', topic: '/fedMsgTopic', username: 'currentUser')
msgComplete = fedMsgComplete(header: header)
msgError = fedMsgError(header: header)

ciPipeline(buildPrefix: 'package-builds', completeMsg: msgComplete, errorMsg: msgError) {
    stage('koji-build') {
        handlePipelineStep(queuedMsg: queuedMsg, runningMsg: runningMsg) {
            executeInContainer(containerName: 'koji-build-container, containerScript: 'verify-build.sh',
                    stageVars: stageVars)
            }
        }
    }
}
```