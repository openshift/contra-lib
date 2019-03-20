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
