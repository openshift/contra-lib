import org.centos.contra.pipeline.Utils


/**
 * Jenkins job to listen for changes to a container, build the image and tag it with the PR #.
 * @param parameters
 * Usage:
 * stageTrigger(containers: ['rpmbuild', image-compose'], scheduledJob: 'fedora-rawhide-build')
 * @return
 */
def call(Map parameters = [:]) {
    def containers = parameters.get('containers', [])
    def scheduledJob = parameters.get('scheduledJob')
    def openshift_namespace = parameters.get('openshift_namespace', 'continuous-infra')
    def docker_repo_url = parameters.get('docker_repo_url', 'docker-registry.default.svc:5000')
    def jenkins_slave_image = parameters.get('jenkins_slave_image', 'jenkins-continuous-infra-slave:stable')

    def tagMaps = [:]
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
                                tagMaps[container] = 'stable'
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
                steps {
                    script {
                        scheduleBuild(buildName: scheduleBuild, params: tagMaps)
                    }
                }
            }
        }
    }
}