/**
 * requires: buildVars['package_name']
 * optional: buildVars['displayName'], buildVars['buildDescription']
 * Example Usage:
 *
 * ciPipeline(buildPrefix: 'fedora-pipeline', buildVars: [package_name: 'vim']) {
 *     stage('run-job') {
 *         handlePipelineStep {
 *             runCode()
 *         }
 *     }
 * }
 *
 * @param parameters
 * @param body
 * @return
 */

def call(Map parameters, Closure body) {
    def buildPrefix = parameters.get('buildPrefix', 'contra-pipeline')
    def buildVars = parameters.get('buildVars', [:])

    def jobMeasurement = "${buildPrefix}-${env.JOB_NAME}"
    def packageMeasurement = "${buildPrefix}-${buildVars['package_name']}"

    try {
        body()
    } catch(e) {
        // Set build result
        currentBuild.result = "FAILURE"

        echo e.getMessage()

        throw e
    } finally {
        currentBuild.result = currentBuild.result ?: 'SUCCESS'
        if (currentBuild.result == 'SUCCESS') {
            step([$class: 'ArtifactArchiver', allowEmptyArchive: true,
                  artifacts: '**/logs/**,*.txt,*.groovy,**/job.*,**/*.groovy,**/inventory.*', excludes: '**/job.props,**/job.props.groovy,**/*.example,**/*.qcow2',
                  fingerprint: true])
        } else {
            step([$class: 'ArtifactArchiver', allowEmptyArchive: true,
                  artifacts: '**/logs/**,*.txt,*.groovy,**/job.*,**/*.groovy,**/inventory.*,**/*.qcow2', excludes: '**/job.props,**/job.props.groovy,**/*.example',
                  fingerprint: true])
        }

        currentBuild.displayName = buildVars['displayName'] ?: "Build #${env.BUILD_NUMBER}"
        currentBuild.description = buildVars['buildDescription'] ?: currentBuild.result

        this.ciMetrics.setMetricTag(jobMeasurement, 'package_name', buildVars['package_name'])
        this.ciMetrics.setMetricTag(jobMeasurement, 'build_result', currentBuild.result)
        this.ciMetrics.setMetricField(jobMeasurement, 'build_time', currentBuild.getDuration())
        this.ciMetrics.setMetricField(packageMeasurement, 'build_time', currentBuild.getDuration())
        this.ciMetrics.setMetricTag(packageMeasurement, 'package_name', buildVars['package_name'])
        //this.ciMetrics.writeToInflux()
    }

}
