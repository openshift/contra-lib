import org.centos.contra.pipeline.Utils

/**
 * Define containers to deploy to openshift
 * Example Usage in Jenkinsfile:
 *
 * ocContainersWithProps = ['rpmbuild':  [tag: 'latest',
 *                                        command: 'cat',
 *                                        privileged: false]]
 * deployOpenShiftTemplate(containers: ocContainers, podName: "fedora-${UUID.randomUUID().toString()}") {
 *     deployCode()
 * }
 * 
 * @param parameters
 * containers: List - A list of containers to deploy
 * containersWithProps: Map - A map with the container name as the key and values tag, command and privileged
 * openshift_namespace: String - The namespace the containers are running in
 * docker_repo_url: String - The address:port of the docker registry
 * podName: String - The name of the pod to run the containers in
 * openshift_service_account: String - The openshift service account
 * jenkins_slave_image: String - Container:tag of the jenkins slave container
 * @param body
 * @return
 */
def call(Map parameters = [:], Closure body) {

    sh 'env'

    def utils = new Utils()

    timestamps {

        def ocContainers = parameters.get('containers', [])
        def ocContainersWithProps = parameters.get('containersWithProps', [:])
        def openshift_namespace = parameters.get('openshift_namespace', (env.OPENSHIFT_BUILD_NAMESPACE ?: 'continuous-infra'))
        def docker_repo_url = parameters.get('docker_repo_url', 'docker-registry.default.svc:5000')
        def podName = parameters.get('podName', "${env.BUILD_TAG}-${UUID.randomUUID().toString()}")
        def openshift_service_account = parameters.get('openshift_service_account', (env.oc_serviceaccount_name ?: 'jenkins'))
        def jenkins_slave_image = parameters.get('jenkins_slave_image', 'jenkins-continuous-infra-slave:stable')

        def containerTemplates = []

        // add default jenkins slave container
        containerTemplates << containerTemplate(name: 'jnlp',
                image: "${docker_repo_url}/${openshift_namespace}/${jenkins_slave_image}",
                ttyEnabled: false,
                args: '${computer.jnlpmac} ${computer.name}',
                command: '',
                workingDir: '/workDir')

        ocContainers.each { containerName ->
            def tag = 'stable'
            def cmd = 'cat'
            def imageUrl = "${docker_repo_url}/${openshift_namespace}/${containerName}:${tag}"
            containerTemplates << containerTemplate(name: containerName,
                    alwaysPullImage: true,
                    image: imageUrl,
                    ttyEnabled: true,
                    command: cmd,
                    privileged: true,
                    workingDir: '/workDir')
        }

        ocContainersWithProps.each { containerName, containerProps ->
            def tag = containerProps.get('tag', 'stable')
            def cmd = containerProps.get('command', 'cat')
            def privileged = containerProps.get('privileged', true)
            def imageUrl = "${docker_repo_url}/${openshift_namespace}/${containerName}:${tag}"

            containerTemplates << containerTemplate(name: containerName,
                    alwaysPullImage: true,
                    image: imageUrl,
                    ttyEnabled: true,
                    command: cmd,
                    privileged: privileged,
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

                utils.verifyPod(openshift_namespace)

                body()
            }
        }
    }
}
