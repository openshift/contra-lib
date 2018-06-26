# A general library for upstream ci

#### Example Usage:
```
package_name = env.CI_MESSAGE['name']
msgHeader = fedMsgHeader(branch: 'fed_repo', topic: '/fedMsgTopic', username: 'currentUser')
msgComplete = fedMsgComplete(header: header)
msgError = fedMsgError(header: header)

// containers to be deployed
containers = ['rpmbuild', 'singlehost-test']

// deploy an openshift pod template with containers
deployOpenShiftTemplate(containers: containers) {

    /* wrap the whole pipeline in a try/catch block
       this will also handle:
       - sending a complete and error fed msg
       - archving artifacts
       - sending pipeline metrics to influxdb
    */
    ciPipeline(buildPrefix: 'package-builds', completeMsg: msgComplete, errorMsg: msgError, package_name: package_name) {
    
        stage('koji-build') {
            // wrap the stage in extra debugging information.
            // plus time how long it takes to run the stage
            handlePipelineStep() {
                executeInContainer(containerName: 'koji-build-container, containerScript: 'verify-build.sh')
            
            }
        }
        
        stage('package-tests') {
            // define variables that will be used by the container: singlehost-test
            stageVars = [repo: 'myrepo', test-all: true]
            handlePipelineStep() {
                executeInContainer(containerName: 'singlehost-test, containerScript: 'package-tests.sh',
                stageVars: stageVars)
           
            }
         }
         
    }
}
```