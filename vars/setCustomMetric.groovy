import org.centos.contra.pipeline.ciMetrics

/*
Call this to set metrics outside of stage and job build time
This must be called within ciPipeline

ciPipeline {
    setCustomMetric(customTags: [company_name: 'xyzCompany'])
}
 */

def call(Map parameters = [:]) {
    def cimetrics = ciMetrics.metricsInstance

    def customTags = parameters.get('customTags', [:])
    def customFields = parameters.get('customFields', [:])

    cimetrics.setMetricTags(env.JOB_NAME, customTags)
    cimetrics.setMetricFields(env.JOB_NAME, customFields)

}