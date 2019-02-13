def call(Map parameters = [:]) {
    def container = parameters.get('container', 'singlehost-test')
    def imageName = parameters.get('imageName')
    def interactions = parameters.get('interactions', '10')

    def prep_cmd = """
              yum install -y python-pip && \
              pip install requests && \
              exit \$?
              """

    def cmd = """
              curl -O https://pagure.io/upstream-fedora-ci/raw/master/f/validate-test-subject.py && \
              rm -rf /tmp/artifacts && \
              python -u validate-test-subject.py -i ${interactions} -s \$(pwd)/${imageName} && \
              exit \$?
              """

    handlePipelineStep() {
        // retry to install packages as there could be some temporary infra issue
        retry(10) {
            executeInContainer(containerName: container, containerScript: prep_cmd)
        }
        executeInContainer(containerName: container, containerScript: cmd)
    }
}
