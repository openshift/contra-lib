/**
 * Create a build of an image in OpenShift
 * @params parameters
 * openshiftProject: The OpenShift project name to work in
 * buildConfig: The OpenShift image stream name (which also should be the build name)
 * tag: The tag to push the new image to. Defaults to latest
 * @return
 */

def call(Map parameters = [:]) {
    def openshiftProject = parameters.get('openshiftProject')
    def buildConfig = parameters.get('buildConfig')
    def tag = parameters.get('tag', 'latest')

    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            def result = openshift.startBuild(buildConfig,
                    "--wait")
            def out = result.out.trim()
            echo "Resulting Build: " + out

            def describeStr = openshift.selector(out).describe()
            out = describeStr.out.trim()

            // --wait is being lost due to socket timeouts
            buildRunning = true
            while (buildRunning) {
                describeStr = openshift.selector(out).describe()
                outTrim = describeStr.out.trim()
                buildRunning = sh(script: "echo \"${outTrim}\" | grep '^Status:' | grep -E 'New|Pending|Running'", returnStatus: true) == 0
                sleep 60
            }

            def imageHash = sh(
                    script: "echo \"${out}\" | grep 'Image Digest:' | cut -f2- -d:",
                    returnStdout: true
            ).trim()
            echo "imageHash: ${imageHash}"

            echo "Creating ${tag} tag for ${openshiftProject}/${buildConfig}"

            openshift.tag("${openshiftProject}/${buildConfig}@${imageHash}",
                    "${openshiftProject}/${buildConfig}:${tag}")

        }
    }
}
