#!groovy
package org.contralib

import com.cloudbees.groovy.cps.NonCPS

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

@Singleton
class ciMetrics {

    // A map to store the data sent to influx
    public def customDataMap = [:]
    // Global tags
    public def customDataMapTags = [:]
    // This will prefix the data sent to influx. Usually set to the job name.
    public def prefix = "ci_pipeline"
    // The influx target configured in jenkins
    public def influxTarget = "localInflux"

    /**
     * Call this method to record the step run time
     * @param name - the step name
     * @param body - the enclosing step body
     */
    @NonCPS
    def timed(String measurement, String name, Closure body) {
		def start = System.currentTimeMillis()

		body()

		def now = System.currentTimeMillis()

        setMetricField(measurement, name, now - start)
    }

    /**
     * Method used to set fields in an influxdb measurement
     * @param measurement
     * @param key
     * @param value
     * @return
     */
    @NonCPS
    def setMetricField(String measurement, String key, def value) {
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
    @NonCPS
    def setMetricTag(String measurement, String key, String value) {
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
    @NonCPS
    def setMetricTags(String measurement, Map tags) {
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
    @NonCPS
    def setMetricFields(String measurement, Map fields) {
        if (!customDataMap[measurement]) {
            customDataMap[measurement] = [:]
        }

        customDataMap[measurement] = customDataMap[measurement] + fields
    }


    /**
     * Write customDataMap to influxDB
     */
    /*
    def writeToInflux(def script) {
        script.step([$class: 'InfluxDbPublisher',
            customData: [:],
            customDataMap: customDataMap,
            customDataMapTags: customDataMapTags,
            customPrefix: prefix,
            target: influxTarget])
 
    }
    */
}

