def call(Map parameters = [:]) {
    def container = parameters.get('container', 'singlehost-test')
    def stageVars = [:]
    stageVars['imageName'] = parameters.get('imageName')
    stageVars['increase'] = parameters.get('increase')


    executeInContainer(containerName: container, containerScript: '/tmp/resize-qcow2.sh', stageVars: stageVars)
}
