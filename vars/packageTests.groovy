/**
 * Requires a message with variables:
 * - branch: the branch of the repo
 * - repo: the package name
 * - test_subjects: 
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'singlehost-test')
    def command = parameters.get('command', '/tmp/package-test.sh')

    def stageVars = [:]
    stageVars['branch'] = parameters.get('branch')
    stageVars['rpm_repo'] = parameters.get('rpm_repo')
    stageVars['package'] = parameters.get('package')
    stageVars['TEST_SUBJECTS'] = parameters.get('test_subjects')

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
