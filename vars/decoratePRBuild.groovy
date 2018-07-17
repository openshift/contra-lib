def call() {
    return {
        if ((env.CHANGE_BRANCH) && (env.CHANGE_AUTHOR)) {
            currentBuild.displayName = "Build #${env.BUILD_ID} - Author: ${env.CHANGE_AUTHOR} - Branch: ${env.CHANGE_BRANCH}"
        } else {
            currentBuild.displayName = "Build #${env.BUILD_ID}"
        }
        currentBuild.description = currentBuild.getResult()
    }
}