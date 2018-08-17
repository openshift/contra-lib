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
```groovy
deployOpenShiftTemplate(containers: ['rpmbuild-container'], openshift_namespace: 'default')
```
#### ciPipeline
This function wraps the whole pipeline in a try/catch/finally block while accepting parameters to initialize and tear down
the pipeline.
Parameters:
- buildPrefix: A prefix to set that describes the build. This is mainly used for metrics.
- package_name: If building a package like an RPM set this to its name. e.g. 'vim'
- errorMsg: A fedMsgError to send on pipeline failure.
- completeMsg: A fedMsgComplete to send on pipeline completion.
- decorateBuild: A Closure that decorates the build such as the function decoratePRBuild()
- postBuild: A Closure that contains any post build steps. e.g. ArtifactArchiver step.
- timeout: Set to time the pipeline out after timeout minutes. Defaults to 30.
- sendMetrics: Whether to send metrics to influxdb. true or false.
```groovy
ciPipeline(buildPrefix: 'myrpmbuilder', decorateBuild: {currentBuild.displayName: 'env.BUILD_ID'})
```
#### executeInContainer
This function executes a script in a container. It's wrapped by handlePipelineStep.
Parameters:
- containerName: The name of the container to use. This corresponds to the container name set in deployOpenShiftTemplate.
- containerScript: The shell command to run.
- stageVars: A Map containing the environment variables to pass to the container.
- loadProps: Properties to load from a previously run stage. This accepts a list of stage names and will load a properties file
from ${stageName}/job.props.
- credentials: Credentials to pass to the container as environment variables. This accepts a list of credentials loaded
from Jenkins
```groovy
executeInContainer(containerName: 'rpmbuild-container', containerScript: 'echo success', stageVars: ['var1': 'val1'],
                        credentials: credentials)
```

#### stageTrigger
Jenkins job to listen for changes to a container, build the image and tag it with the PR #.
```groovy
stageTrigger(containers: ['rpmbuild', image-compose'], scheduledJob: 'fedora-rawhide-build')
```

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
