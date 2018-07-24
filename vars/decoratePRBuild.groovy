def call(Map parameters = [:]) {
    def change_branch = parameters.get('change_branch', env.CHANGE_BRANCH)
    def change_author = parameters.get('change_author', env.CHANGE_AUTHOR)
    def tag_name = parameters.get('tag_name', env.TAG_NAME)

    return {
        if ((change_branch) && (change_author)) {
            currentBuild.displayName = "Build #${env.BUILD_ID} - Author: ${change_author} - Branch: ${change_branch}"
        } else if (tag_name) {
            currentBuild.displayName = "Build #${env.BUILD_ID} - Branch: ${tag_name}"
        } else {
            currentBuild.displayName = "Build #${env.BUILD_ID}"
        }
        currentBuild.description = currentBuild.getResult()
    }
}