def call(Map parameters = [:]) {
    def container = parameters.get('container', 'rpmbuild')
    def command = parameters.get('command', '/tmp/koji_build_pr.sh')
    def message = parameters.get('message', [:])
    def loadContainerVars = parameters.get('loadContainerVars', true)

    def stageVars = [:]
    stageVars['fed_repo'] = message['repo']
    stageVars['fed_id'] = message['id']
    stageVars['fed_uid'] = message['uid']

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
