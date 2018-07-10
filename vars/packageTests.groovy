/**
 * Requires a message with variables:
 * - branch: the branch of the repo
 * - package: the package name
 * @param parameters
 * @return
 */


def call(Map parameters = [:]) {
    def container = parameters.get('container', 'singlehost-test')
    def command = parameters.get('command', '/tmp/package-test.sh')

    def stageVars = [:]
    stageVars['branch'] = parameters.get('branch')
    stageVars['package'] = parameters.get('package')
    stageVars['rpm_repo'] = parameters.get('rpm_repo', "${env.WORKSPACE}/${stageVars['package']}_repo")
    stageVars['TEST_SUBJECTS'] = parameters.get('test_subjects', 'test_subject.qcow2')

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: command, stageVars: stageVars)
    }
}
