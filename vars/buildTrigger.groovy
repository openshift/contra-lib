def call(Map parameters, Closure body) {

    def stageName = parameters.get('stageName')
    def buildScript = parameters.get('buildScript')

    def result = false
    handlePipelineStep(name: stageName) {

        body()

        result = buildScript.executeTrigger()
    }

    return result
}
