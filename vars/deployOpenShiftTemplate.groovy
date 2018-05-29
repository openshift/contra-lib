/**
 * Define containers to deploy to openshift
 * Example Usage in Jenkinsfile:
 *
 * ocContainers = ['rpmbuild': [tag: 'latest']]
 * deployOpenShiftTemplate(containers: ocContainers, podName: "fedora-${UUID.randomUUID().toString()}") {
 *     deployCode()
 * }
 * 
 * @param parameters
 * @param body
 * @return
 */
def call(Map parameters, Closure body) {

    timestamps {

        def ocContainers = parameters.get('containers', [:])
        def openshift_namespace = parameters.get('openshift_namespace', 'continuous-infra')
        def docker_repo_url = parameters.get('docker_repo_url', 'docker-registry.default.svc:5000')
        def podName = parameters.get('podName', "generic-${UUID.randomUUID().toString()}")
        def openshift_service_account = parameters.get('openshift_service_account', 'jenkins')
        def jenkins_slave_image = parameters.get('jenkins_slave_image', 'jenkins-continuous-infra-slave:stable')

        def containerTemplates = []

        // add default jenkins slave container
        containerTemplates << containerTemplate(name: 'jnlp',
                image: "${docker_repo_url}/${openshift_namespace}/${jenkins_slave_image}",
                ttyEnabled: false,
                args: '${computer.jnlpmac} ${computer.name}',
                command: '',
                workingDir: '/workDir')

        ocContainers.each { containerName, containerProps ->
            def tag = containerProps.get('tag', 'stable')
            def imageUrl = "${docker_repo_url}/${openshift_namespace}/${containerName}:${tag}"
            containerTemplates << containerTemplate(name: containerName,
                    alwaysPullImage: true,
                    image: imageUrl,
                    ttyEnabled: true,
                    command: 'cat',
                    privileged: true,
                    workingDir: '/workDir')
        }

        podTemplate(name: podName,
                label: podName,
                cloud: 'openshift',
                serviceAccount: openshift_service_account,
                idleMinutes: 0,
                namespace: openshift_namespace,
                containers: containerTemplates,
                volumes: [emptyDirVolume(memory: false, mountPath: '/sys/class/net')]
        ) {
            node(podName) {
                body()
            }
        }
    }
}
