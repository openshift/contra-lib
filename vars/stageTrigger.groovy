import org.contralib.Utils


def call(Map parameters = [:]) {
    def containers = parameters.get('containers', [:])
    def tagMaps = parameters.get('tagMaps', [:])
    def scheduledJob = parameters.get('scheduledJob')
    def openshift_namespace = parameters.get('openshift_namespace', 'continuous-infra')
    def docker_repo_url = parameters.get('docker_repo_url', 'docker-registry.default.svc:5000')
    def jenkins_slave_image = parameters.get('jenkins_slave_image', 'jenkins-continuous-infra-slave:stable')


    def utils = new Utils()

    pipeline {
        agent {
            kubernetes {
                cloud 'openshift'
                label "stage-trigger-${env.JOB_NAME}"
                containerTemplate {
                    name 'jnlp'
                    args '${computer.jnlpmac} ${computer.name}'
                    image "${docker_repo_url}/${openshift_namespace}/${jenkins_slave_image}"
                    ttyEnabled false
                    command ''
                }
            }
        }

        stages {
            stage("Getting Changelogs") {
                steps {
                    node('master') {
                        script {
                            containers.each { container ->
                                stage("${container} image build") {
                                    when {
                                        changeset "config/Dockerfiles/${container}/**"
                                    }

                                    tagMaps[container] = utils.buildImage(openshift_namespace, container)
                                }
                            }
                        }
                    }
                }
            }
            stage("Schedule Build") {
                scheduleBuild(buildName: scheduleBuild, params: tagMaps)
            }
        }
    }
}