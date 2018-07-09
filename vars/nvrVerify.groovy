/**
 * Required variables:
 * - branch: branch name in the message
 * - rpm_repo: the repo directory
 * - package: the package name being tested
 * @param parameters
 * @return
 */

def call(Map parameters = [:]) {
    def container = parameters.get('container', 'singlehost-test')
    def command = parameters.get('command', '/tmp/verify-rpm.sh')

    def stageVars = [:]
    stageVars['branch'] = parameters.get('branch')
    stageVars['rpm_repo'] = parameters.get('rpm_repo')
    stageVars['package'] = parameters.get('package')
    stageVars['python3'] = parameters.get('python3', true)
    stageVars['TAG'] = parameters.get('tag', 'classic')
    stageVars['build_pr_id'] = parameters.get('build_pr_id', '')

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
