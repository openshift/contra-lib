#!/usr/bin/env groovy

import com.lesfurets.jenkins.unit.global.lib.Library

@Library('contra-lib@master')


def execute() {
    node() {
        ciPipeline(sendMetrics: false) {
            stage("First") {
                decorateBuild()
            }
        }
    }
}

return this
