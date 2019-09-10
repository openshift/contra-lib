/**
 * Create a build of an image in OpenShift
 * @params parameters
 * openshiftProject: The OpenShift project name to work in
 * buildConfig: The OpenShift image stream name (which also should be the build name)
 * tag: The tag to push the new image to. Defaults to latest
 * pullId: Specify pull request ID for buildConfig. Defaults to null
 * @return
 */

def call(Map parameters = [:]) {
    def openshiftProject = parameters.get('openshiftProject')
    def buildConfig = parameters.get('buildConfig')
    def tag = parameters.get('tag', 'latest')
    def pullId = parameters.get('pullId')

    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            def result = null
            if (!pullId) {
                result = openshift.startBuild(buildConfig,
                    "--wait")
            } else {
                result = openshift.startBuild(buildConfig,
                    "--commit",
                    "refs/pull/" + pullId + "/head",
                    "--wait")
            }
            def out = result.out.trim()
            echo "Resulting Build: " + out

            def outTrim = ""

            // --wait is being lost due to socket timeouts
            buildRunning = true
            while (buildRunning) {
                describeStr = openshift.selector(out).describe()
                outTrim = describeStr.out.trim()
                buildRunning = sh(script: "echo \"${outTrim}\" | grep '^Status:' | grep -E 'New|Pending|Running'", label: "Checking build status", returnStatus: true) == 0
                sleep 60
            }

            def imageHash = sh(
                    script: "echo \"${outTrim}\" | grep 'Image Digest:' | cut -f2- -d:",
                    label: "Getting Image Hash",
                    returnStdout: true
            ).trim()
            echo "imageHash: ${imageHash}"

            echo "Creating ${tag} tag for ${openshiftProject}/${buildConfig}"

            openshift.tag("${openshiftProject}/${buildConfig}@${imageHash}",
                    "${openshiftProject}/${buildConfig}:${tag}")

        }
    }
}
