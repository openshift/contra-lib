/**
 * Requires a message with variables:
 * - repo: package name in the message
 * @param parameters
 * @return
 */

def call(Map parameters = [:]) {
    def container = parameters.get('container', 'rpmbuild')
    def command = parameters.get('command', '/tmp/repoquery.sh')
    def ciMessage = parameters.get('ciMessage', [:])

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: ['fed_repo': ciMessage['repo']])
    }
}