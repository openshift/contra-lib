/**
 * Required variables:
 * - branch: the branch of the repo
 * - repo: the package name
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'cloud-image-compose')
    def command = parameters.get('command', '/tmp/virt-customize.sh')

    def stageVars = [:]
    stageVars['branch'] = parameters.get('branch')
    stageVars['rpm_repo'] = parameters.get('rpm_repo')
    stageVars['package'] = parameters.get('package')

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
