/**
 * Requires a message with variables:
 * - branch: the branch of the repo
 * - repo: the package name
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'cloud-image-compose')
    def command = parameters.get('command', '/tmp/virt-customize.sh')
    def ciMessage = parameters.get('ciMessage', [:])

    handlePipelineStep() {
        def stageVars = [:]
        stageVars['branch'] = ciMessage['branch']
        stageVars['rpm_repo'] = "${ciMessage['repo']}_repo"
        stageVars['package'] = ciMessage['repo']

        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}