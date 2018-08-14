/**
 * Send data to influxdb
 *
 * Example Usage:
 *
 * writeToInflux(customData: [build_time: 100], customDataMap['mybuild': ['build_time': 100]])
 *
 * @param parameters
 * @return
 */

def call(Map parameters = [:]) {
    def customData = parameters.get('customData', [:])
    def customDataTags = parameters.get('customDataTags', [:])
    def customDataMap = parameters.get('customDataMap', ['ci_pipeline': [:]])
    def customDataMapTags = parameters.get('customDataMapTags', ['ci_pipeline': [:]])
    def customPrefix = parameters.get('customPrefix', 'ci_pipeline')
    def target = parameters.get('target', 'localInflux')

    print customDataMapTags
    print customDataMap
    
    step([$class: 'InfluxDbPublisher',
            customData: customData,
            customDataTags: customDataTags,
            customDataMap: customDataMap,
            customDataMapTags: customDataMapTags,
            customPrefix: customPrefix,
            target: target
    ])
}
