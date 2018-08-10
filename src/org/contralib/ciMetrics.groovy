#!groovy
package org.contralib


/*
A class to store build metrics over the lifetime of the build.
Metrics are stored in customDataMap and then sent to influx at
the end of the job. Example usage:

try {
    def stepName = "mystep"
    ciMetrics.timed stepName, {
        stage(stepName) {
            echo "in mystep"
        }
    }
    currentBuild.result = "SUCCESS"
} catch(err) {
    currentBuild.result = "FAILED"
    throw err
} finally {
    ciMetrics.writeToInflux()
}
 */


class ciMetrics {

    def static final metricsInstance = new ciMetrics()

    // A map to store the data sent to influx
    def customDataMap = [:]
    // Global tags
    def customDataMapTags = [:]
    // This will prefix the data sent to influx. Usually set to the job name.
    def prefix = "ci_pipeline"
    // The influx target configured in jenkins
    def influxTarget = "localInflux"


    /**
     * Call this method to record the step run time
     * @param name - the step name
     * @param body - the enclosing step body
     */
    def timed(String measurement, String name, Closure body) {
		def start = System.currentTimeMillis()
        def total_time = 0.0
        try {
            body()
        } catch(e) {
            throw e
        } finally {
            def now = System.currentTimeMillis()
            total_time = now - start
            setMetricField(measurement, name, total_time)
        }

        return total_time
    }

    /**
     * Method used to set fields in an influxdb measurement
     * @param measurement
     * @param key
     * @param value
     * @return
     */
    def setMetricField(String measurement, String key, def value) {
        measurement = "${prefix}-${measurement}"
        if (!customDataMap[measurement]) {
            customDataMap[measurement] = [:]
        }

        customDataMap[measurement][key] = value
    }

    /**
     * Method used to set tags in an influxdb measurement
     * @param measurement
     * @param key
     * @param value
     * @return
     */
    def setMetricTag(String measurement, String key, String value) {
        measurement = "${prefix}-${measurement}"
        if (!customDataMapTags[measurement]) {
            customDataMapTags[measurement] = [:]
        }

        customDataMapTags[measurement][key] = value
    }

    /**
     * Add multiple tags
     * @param measurement
     * @param tags
     * @return
     */
    def setMetricTags(String measurement, Map tags) {
        measurement = "${prefix}-${measurement}"
        if (!customDataMapTags[measurement]) {
            customDataMapTags[measurement] = [:]
        }

        customDataMapTags[measurement] = customDataMapTags[measurement] + tags
    }

    /**
     * Add multiple fields
     * @param measurement
     * @param fields
     * @return
     */
    def setMetricFields(String measurement, Map fields) {
        measurement = "${prefix}-${measurement}"
        if (!customDataMap[measurement]) {
            customDataMap[measurement] = [:]
        }

        customDataMap[measurement] = customDataMap[measurement] + fields
    }
}

