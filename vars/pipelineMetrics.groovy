import org.centos.contra.pipeline.ciMetrics


def call(Map parameters = [:]) {
    def cimetrics = ciMetrics.metricsInstance
    def buildPrefix = parameters.get('buildPrefix')
    def package_name = parameters.get('package_name')
    def customDataMap = parameters.get('customDataMap', [:])
    def customDataMapTags = parameters.get('customDataMapTags', [:])
    def customData = parameters.get('customData', [:])
    def customDataTags = parameters.get('customDataTags', [:])

    if (buildPrefix) {
        cimetrics.prefix = buildPrefix
    }

    if (package_name) {
        cimetrics.setMetricTag(env.JOB_NAME, 'package_name', package_name)
        cimetrics.setMetricField(package_name, 'build_time', currentBuild.getDuration())
        cimetrics.setMetricTag(package_name, 'package_name', package_name)
    }

    customDataMap.each { measurement, values ->
        cimetrics.setMetricFields(measurement, values)
    }

    customDataMapTags.each { measurement, values ->
        cimetrics.setMetricTags(measurement, values)
    }

    customData.each { key, value ->
        cimetrics.customData[key] = value
    }

    customDataTags.each { key, value ->
        cimetrics.customDataTags[key] = value
    }

    cimetrics.setMetricTag(env.JOB_NAME, 'build_result', currentBuild.currentResult)
    cimetrics.setMetricField(env.JOB_NAME, 'build_time', currentBuild.getDuration())

    writeToInflux(customDataMap: cimetrics.customDataMap,
                  customDataMapTags: cimetrics.customDataMapTags,
                  customData: cimetrics.customData,
                  customDataTags: cimetrics.customDataTags,
                  customPrefix: buildPrefix)
}
