# A general library for upstream ci

#### Example Usage:
```
stageVars = [repo: 'vim']
ciPipeline(buildPrefix: 'package-builds') {
    stage('koji-build') {
        handlePipelineStep(queuedMsg: queuedMsg, runningMsg: runningMsg) {
            executeInContainer(containerName: 'koji-build-container, containerScript: 'verify-build.sh',
                    stageVars: stageVars)
            }
        }
    }
}
```