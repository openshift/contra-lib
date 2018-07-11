/**
 * Requires a message with variables:
 * - task_id: the koji task id in the message
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'rpmbuild')
    def command = parameters.get('command', '/tmp/pull_old_task.sh')

    def stageVars = [:]
    stageVars['PROVIDED_KOJI_TASKID'] = parameters.get('task_id')

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
