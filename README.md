# Jenkins shared library

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
the pipeline.<br>
If env.topicPrefix is defined, the ciPipeline function will attempt
to send out a message on env.topicPrefix.pipeline.running when it begins
and one on env.topicPrefix.pipeline.<complete,error> when it finishes,
based on the return code. The success of sending these messages
depend on the other environment variables listed in the ciStage section
being defined. However, no extra parameter need be passed as an argument
to ciPipeline.

\* See Required scriptApprovals for Messaging

Parameters:
- buildPrefix: A prefix to set that describes the build. This is mainly used for metrics.
- package_name: If building a package like an RPM set this to its name. e.g. 'vim'
- errorMsg: A msgBusTestError to send on pipeline failure.
- completeMsg: A msgBusTestComplete to send on pipeline completion.
- decorateBuild: A Closure that decorates the build such as the function decoratePRBuild()
- preBuild: A closure that contains pre build steps.
- postBuild: A Closure that contains any post build steps. e.g. ArtifactArchiver step.
- timeout: Set to time the pipeline out after timeout minutes. Defaults to 30.
- sendMetrics: Whether to send metrics to influxdb. true or false.
```groovy
ciPipeline(buildPrefix: 'myrpmbuilder', decorateBuild: {currentBuild.displayName: 'env.BUILD_ID'})
```
#### ciStage
This function wraps a stage to run the steps preceded by sending
a conforming message on the pipeline.stage.running topic and
followed by a message on the pipeline.stage.complete topic.<br>
The one argument it takes is the stage name (string).<br>
This function only works if the required environment variables
to populate the two messages are defined. These variables are:
- env.topicPrefix: The prefix of the message topic. Choices include org.fedoraproject.prod.ci and VirtualTopic.eng.ci
- env.effortName: Name of the effort or pipeline
- env.teamName: Name of the team responsible for the pipeline
- env.teamEmail: Mailing list for the team responsible for the pipeline
- env.pipelineId: UUID for this pipeline run
- env.MSG_PROVIDER: This is actually required by the sendMessage function that ciStage uses. It is the provider configured in Jenkins on which to send messages.
- env.dataGrepperUrl: This is required by the sendMessageWithAudit function that ciStage uses. It tells the function where to look to confirm the message was sent as expected.

The following variables are optional:
- env.teamIRC: IRC channel for team responsible for this pipeline. Can be left out
- env.pipelineName: For use if there is a desire for a name for the pipeline differing from the name of the effort. Defaults to env.effortName

\* See Required scriptApprovals for Messaging

Example usage (setEnvVars need only be done once per Jenkinsfile):
```groovy
setEnvVars(topicPrefix: 'org.fedoraproject.prod.ci', effortName: 'someEffort', teamName: 'ateam', teamEmail: 'team@org.com', pipelineId: 'teamPipeline', teamIRC: '#team', pipelineName: 'my-pipeline', MSG_PROVIDER: 'fedmsg', dataGrepperUrl: 'https://apps.fedoraproject.org/datagrepper')
ciStage('my-stage') {
    println('I am running in a stage')
}
ciStage('my-other-stage') {
    println('I am running in another stage')
}
```
#### sendPipelineStatusMsg
The intent of this function is to enable the messaging aspects of the
ciPipeline function for declarative pipelines. Due to syntactical
limitations in declarative Jenkinsfiles, there is not a clean way to
utilize ciPipeline. However, sendPipelineStatusMsg can be added to a
pipeline to easily send out proper messages on the
pipeline.[running,complete,error] topics. Again, setting the environment
variables listed in the ciStage section is required.

\* See Required scriptApprovals for Messaging section

Declarative pipeline example usage:
```groovy
pipeline {
    agent {
        label 'master'
    }
    stages {
        stage('Stage 1') {
            steps {
                script {
                    ciStage('Stage 1') {
                        sendPipelineStatusMsg('running')
                    }
                }
            }
        }
    }
    post {
        success {
            sendPipelineStatusMsg('complete')
        }
        failure {
            sendPipelineStatusMsg('error')
        }
    }
}
```
Scripted pipeline example (recommended to use ciPipeline function which includes these functions for free instead):
```groovy
node('master') {
    try {
        sendPipelineStatusMsg('running')
        ciStage('Stage 1') {
            ...
        }
        ...
    } catch (e) {
        sendPipelineStatusMsg('error')
    }
}
```
#### Required scriptApprovals for Messaging
In order to use the message aspects of ciPipeline, the ciStage function, sendPipelineStatusMsg, or any of the msgBus*.groovy functions, the following scriptApprovals must be approved on the Jenkins master:
- method java.lang.Class getSuperclass
- method java.lang.Class isAssignableFrom java.lang.Class
- method java.lang.Class isInstance java.lang.Object
- staticMethod java.lang.Class forName java.lang.String
- staticMethod java.time.Instant now
- new groovy.lang.GroovyRuntimeException
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

#### Metrics
Pipeline metrics are collected throughout the run of a pipeline when using the handlePipelineStep and ciPipeline libraries.
These two libraries will collect basic metrics that include:
- Total build time
- Stage run time
- Build number
- Project name
- Time spent in build queue

In addition to the default collected metrics, you can send custom metrics to Influxdb by calling the writeToInflux library.

```groovy
writeToInflux(customData: ['build_time': 100], customDataMap: ['mybuild': ['build_time': 100]])
```
After the pipeline finishes, the ciPipeline library will send all collected metrics to Influxdb.

#### Example Usage:
```
package_name = env.CI_MESSAGE['name']
msgHeader = msgBusHeader(type: 'koji-build', component: package_name, issuer: 'currentUser', scratch: false, topic: '/fedMsgTopic', id: '000000', nvr: 'foo-1.0-fc27')
msgComplete = msgBusTestComplete(header: header)
msgError = msgBusTestError(header: header)

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
