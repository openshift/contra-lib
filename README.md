# A general library for upstream ci

### Function reference
#### deployOpenShiftTemplate
This function leverages the kubernetes plugin for Jenkins. It deploys a podTemplate and containers.
Parameters:
- ocContainers: A list of containers to deploy, specified by the image name in openshift.
- ocContainersWithProps: A Map with the container name as the key and values of the container tag, privileged (true/false)
and the command to run.
- openshift_namespace: The namespace openshift runs in. Defaults to continuous-infra.
- docker_repo_url: The url of the docker repository. Defaults to docker-registry.default.svc:5000.
- podName: The name of the pod that gets deployed. Defaults to generic-${UUID.randomUUID().toString()}.
- openshift_service_account: The openshift service account. Defaults to jenkins.
- jenkins_slave_image: The jnlp image to use. You must specify image:tag. Defaults to jenkins-continuous-infra-slave:stable.

#### ciPipeline
This function wraps the whole pipeline in a try/catch/finally block while accepting parameters to initialize and tear down
the pipeline.
Parameters:
- buildPrefix: A prefix to set that describes the build. This is mainly used for metrics.
- package_name: If building a package like an RPM set this to its name. e.g. 'vim'
- errorMsg: A fedMsgError to send on pipeline failure.
- completeMsg: A fedMsgComplete to send on pipeline completion.
- decorateBuild: A Closure that decorates the build such as the function decoratePRBuild()
- archiveArtifacts: A Closure that contains an ArtifactArchiver step.
- timeout: Set to time the pipeline out after timeout minutes. Defaults to 30.
- sendMetrics: Whether to send metrics to influxdb. true or false.

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
               executeInContainer(containerName: 'koji-build-container, containerScript: 'verify-build.sh')
           
        }
        
        stage('package-tests') {
            // define variables that will be used by the container singlehost-test
            stageVars = [repo: 'myrepo', test-all: true]
                executeInContainer(containerName: 'singlehost-test, containerScript: 'package-tests.sh',
                stageVars: stageVars)

         }
    }
}
```
