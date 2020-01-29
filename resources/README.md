# Fedora CI Messaging Closures

### What are these?
The json files in this directory define various closures that comply with the
messages defined in https://pagure.io/fedora-ci/messages. Each file has a
corresponding groovy file in the vars/ directory of this repository. The
files describe the multiple keys present in each aspect of the messages.
The type of each is listed as well as whether or not the key is required
in all uses of the closure.

### Why use them?
There are a few benefits to be enjoyed from using these functions.
* Your messages will be checked for correctness
    - Attempting to add a field that is not described in the spec will produce an error
    - Attempting to add a field with a value that is not of the proper type will produce an error
* You will receive some defaults for free
    - The best example of this is the generated_at field, which will be added to your messages automatically by the library
    - Other examples include the run array, which mostly self populates in a Jenkins environment using this library
* Helper closures. There are some functions in this repository (ciStage, ciPipeline) that will send certain conforming messages behind the scenes if you use them, provided the required environment variables are set

### Example Usage
```
println('About to create my koji-build.test.running message')
myCIContent = msgBusCIContent(name: 'user', team: 'my-team', url: 'localhost', email: 'user@me.com')
myArtifactContent = msgBusArtifactContent(type: 'koji-build', issuer: 'me', id: 000000)
myPipelineContent = msgBusPipelineContent(name: 'my-first-pipeline')
myTestContent = msgBusTestContent(type: 'tier0', category: 'functional', namespace: 'user-namespace')
myTestRunningMsg = msgBusTestRunning(ci: myCIContent(), artifact: myArtifactContent(), pipeline: myPipelineContent(), test: myTestContent())
println('My new conforming message is: ' + myTestRunningMsg())
```

### Example ciPipeline and ciStage Jenkinsfile
The below is a minimalistic Jenkinsfile using the ciPipeline and ciStage functions to send metrics.
If you add the variables and use the functions from the following example, you will, for free, have your Jenkins build send out conforming messages for pipeline and stage.
The pipeline messages are on topic pipeline.[running,complete,error] and the stage messages are on topic pipeline.stage.[running,complete].
```
env.effortName = "A New Effort" // Req for Contact Closure
env.teamName = "My Team" // Req for Contact Closure
env.teamEmail = "myteam@email.com" // Req for Contact Closure
env.pipelineId = UUID.randomUUID().toString() // Req for Pipeline Closure
env.MSG_PROVIDER = "Message Bus" // Req for sendMessage function. Name of the messaging provider as set up in your Jenkins master config
env.datagrepperUrl = "https://datagrepper.com" // Req for sendMessageWithAudit. The datagrepper instance for your messaging provider
env.topicPrefix = "VirtualTopic.eng.ci" // The prefix to append pipeline.* to in order to form your final topic to send the message on

// Checkout contra-lib
library identifier: "contra-lib@master",
        retriever: modernSCM([$class: "GitSCMSource", remote: "https://github.com/openshift/contra-lib.git"])

node('master') {
    ciPipeline(sendMetrics: false) {
        ciStage('First stage') {
            println("I'm in a stage that will send metrics")
        }
    }
}
```
