/**
 * Requires a message with variables:
 * - task_id: the koji task id in the message
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'rpmbuild')
    def command = parameters.get('command', '/tmp/koji_build_pr.sh')
    def ciMessage = parameters.get('ciMessage', [:])

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command,
        stageVars: ['PROVIDED_KOJI_TASKID': ciMessage['task_id']])
    }
}
