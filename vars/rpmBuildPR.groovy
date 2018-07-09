/**
 * Requires a message with variables:
 * - repo: package name to build
 * - id: id in the message
 * - uid: uid in the message
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'rpmbuild')
    def command = parameters.get('command', '/tmp/koji_build_pr.sh')

    def stageVars = [:]
    stageVars['fed_repo'] = parameters.get('repo')
    stageVars['fed_id'] = parameters.get('id')
    stageVars['fed_uid'] = parameters.get('uid')

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }

}
