/**
 * Requires a message with variables:
 * - repo: package name in the message
 * @param parameters
 * @return
 */

def call(Map parameters = [:]) {
    def container = parameters.get('container', 'rpmbuild')
    def command = parameters.get('command', '/tmp/repoquery.sh')

    def stageVars = [:]
    stageVars['fed_repo'] = parameters.get('repo')

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
