/**
 * Required variables:
 * - package: package name
 * - branch: the branch of the repo
 * - release: fedora release, eg: f28, rawhide
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'cloud-image-compose')
    def command = parameters.get('command', '/tmp/virt-customize.sh')

    def stageVars = [:]
    stageVars['package'] = parameters.get('package')
    stageVars['branch'] = parameters.get('branch')
    stageVars['fed_branch'] = parameters.get('release')
    stageVars['rpm_repo'] = parameters.get('rpm_repo', "${env.WORKSPACE}/${stageVars['package']}_repo")

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
