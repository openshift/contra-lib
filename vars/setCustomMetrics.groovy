import org.contralib.ciMetrics


def call(Map parameters = [:]) {
    def cimetrics = ciMetrics.metricsInstance

    def measurement = parameters.get('measurement', env.JOB_NAME)
    def customFields = parameters.get('fields', [:])
    def customTags = parameters.get('tags', [:])

    cimetrics.setMetricFields(measurement, customFields)
    cimetrics.setMetricTags(measurement, customTags)
}