#!/usr/bin/env groovy


def call(Map parameters = [:]) {
    def stages = parameters.get('stages', [:])
    def postBuild = parameters.get('postBuild')

    deployOpenShiftTemplate() {

        ciPipeline(postBuild: postBuild, decorateBuild: decoratePRBuild()) {

            stages.each { name, data ->

                stage(name) {
                    data()
                }

            }

        }
    }
}

