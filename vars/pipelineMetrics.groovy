import org.centos.pipeline.ciMetrics


def call(Map parameters = [:]) {
    def cimetrics = ciMetrics.metricsInstance
    def buildPrefix = parameters.get('buildPrefix')
    def package_name = parameters.get('package_name')
    def customFields = parameters.get('customFields', [:])
    def customTags = parameters.get('customTags', [:])

    if (package_name) {
        cimetrics.setMetricTag(env.JOB_NAME, 'package_name', package_name)
        cimetrics.setMetricField(package_name, 'build_time', currentBuild.getDuration())
        cimetrics.setMetricTag(package_name, 'package_name', package_name)
    }

    customFields.each { measurement, values ->
        cimetrics.setMetricFields(measurement, values)
    }

    customTags.each { measurement, values ->
        cimetrics.setMetricTags(measurement, values)
    }

    cimetrics.setMetricTag(env.JOB_NAME, 'build_result', currentBuild.currentResult)
    cimetrics.setMetricField(env.JOB_NAME, 'build_time', currentBuild.getDuration())

    writeToInflux(customDataMap: cimetrics.customDataMap,
                  customDataMapTags: cimetrics.customDataMapTags,
                  customPrefix: buildPrefix)
}