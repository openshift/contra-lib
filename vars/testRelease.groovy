import org.centos.contra.pipeline.GitHubRepo

/**
 * Example:
 *
 * withCredentials(usernamePassword(credentialsId: 'jenkins-github-credentials', username: USERNAME, password: PASSWORD)]) {
 *     testRelease(installCmd: 'install.sh', verifyCmd: 'verifyInstall.sh' version: 'v0.0.1'
 * }
 *
 * @param parameters
 * @return
 */
def call(Map parameters = [:]) {
    def installCmd = parameters.installCmd ?: ""
    def verifyCmd = parameters.verifyCmd ?: ""
    def repo = parameters.repo ?: env.REPO
    def releaseMsg = parameters.releaseMsg ?: "Release of ${repo}"
    def username = parameters.username ?: env.USERNAME
    def password = parameters.password ?: env.PASSWORD
    def version = parameters.version

    def gitRepo = new GitHubRepo(username: username, password: password, repo: repo)

    stage('upload-test-version') {
        def cmd = "twine upload --config-file /tmp/pypirc -r " + env.TEST_PYPI_REPO + " dist/* || echo 'Version already uploaded'"
        executeInContainer(containerName: 'buildah-builder', containerScript: cmd)
    }

    stage('install-module') {
        executeInContainer(containerName: 'buildah-builder', containerScript: installCmd)
    }

    stage('verify-module') {
        executeInContainer(containerName: 'buildah-builder', containerScript: verifyCmd)
    }

    stage('create-release') {
        gitRepo.createRelease(version, releaseMsg)
    }
}
