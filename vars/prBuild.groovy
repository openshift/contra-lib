/*
A full pipeline that takes as a parameter the stages to run
Usage: 
def prStages = [stage1: { sh 'ls' }, stage2: { echo 'in stage2' }]
prBuild(stages: prStages)
*/

def call(Map parameters = [:]) {
    def stages = parameters.get('stages', [:])

    deployOpenShiftTemplate() {

        ciPipeline(decorateBuild: decoratePRBuild()) {

            stages.each { name, data ->

                stage(name) {
                    data()
                }

            }

        }
    }
}

