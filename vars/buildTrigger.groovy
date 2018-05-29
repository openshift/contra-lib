def call(Map parameters, Closure body = {}) {

    def result = false
    handlePipelineStep {
        result = body()
    }

    return result
}
