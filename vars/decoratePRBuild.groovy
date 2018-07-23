def call(Map parameters = [:]) {
    def change_branch = parameters.get('change_branch', env.CHANGE_BRANCH)
    def change_author = parameters.get('change_author', env.CHANGE_AUTHOR)
    return {
        if ((change_branch) && (change_author)) {
            currentBuild.displayName = "Build #${env.BUILD_ID} - Author: ${change_author} - Branch: ${change_branch}"
        } else {
            currentBuild.displayName = "Build #${env.BUILD_ID}"
        }
        currentBuild.description = currentBuild.getResult()
    }
}