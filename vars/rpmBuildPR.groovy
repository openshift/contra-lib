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
    def ciMessage = parameters.get('ciMessage', [:])

    handlePipelineStep() {
        def stageVars = [:]
        stageVars['fed_repo'] = ciMessage['repo']
        stageVars['fed_id'] = ciMessage['id']
        stageVars['fed_uid'] = ciMessage['uid']

        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }

}
