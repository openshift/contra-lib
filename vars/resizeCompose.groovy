def call(Map parameters = [:]) {
    def stageVars = [:]
    stageVars['imageName'] = parameters.get('imageName')
    stageVars['increase'] = parameters.get('increase')

    executeInContainer(containerName: 'singlehost-test', containerScript: '/tmp/resize-qcow2.sh', stageVars: stageVars)
}
