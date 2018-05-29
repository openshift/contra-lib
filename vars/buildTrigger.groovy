def call(Closure body = {}) {

    def result = false
    handlePipelineStep {
        result = body()
    }

    return result
}
