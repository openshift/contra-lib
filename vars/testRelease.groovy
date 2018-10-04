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
    def username = parameters.username ?: env.USERNAME
    def password = parameters.password ?: env.PASSWORD
    def version = parameters.version

    def prTitle = parameters.prTitle ?: "Merge of ${version}"
    def prHead = parameters.prHead ?: 'develop'
    def prBase = parameters.prBase ?: 'master'
    def prBody = parameters.prBody ?: "Merge release ${version}"

    def TEST_PYPI_REPO = parameters.test_pypi_repo ?: 'test-repo'
    def PROD_PYPI_REPO = parameters.prod_pypi_repo ?: 'prod-repo'


    def gitRepo = new GitHubRepo(username: username, password: password, repo: repo)

    stage('upload-test-version') {
        def cmd = """
        python setup.py sdist bdist_wheel
        twine upload --config-file /tmp/pypirc -r " + ${TEST_PYPI_REPO} + " dist/* || echo 'Version already uploaded'
        """
        //executeInContainer(containerName: 'buildah-builder', containerScript: cmd)
    }

    stage('install-module') {
        //executeInContainer(containerName: 'buildah-builder', containerScript: installCmd)
    }

    stage('verify-module') {
        //executeInContainer(containerName: 'buildah-builder', containerScript: verifyCmd)
    }

    stage('release-prod') {
        def cmd = """
        twine upload --config-file /tmp/pypirc -r " + ${PROD_PYPI_REPO} + " dist/*
        """
        //executeInContainer(containerName: 'buildah-builder', containerScript: cmd)

        print "creating PR"
        print prTitle
        print prHead
        print prBase
        print prBody
        def pullRequest = gitRepo.createPR(prTitle, prHead, prBase, prBody)
        print "created PR"
        print "rebasing PR"
        print pullRequest.getClass()
        gitRepo.rebasePR(pullRequest)
        print "rebased PR"

    }
}
