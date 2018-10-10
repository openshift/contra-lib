def call(Map parameters = [:]) {
    def container = parameters.get('container', 'singlehost-test')
    def imageName = parameters.get('imageName')

    def cmd = """
              curl -O https://pagure.io/upstream-fedora-ci/raw/master/f/validate-test-subject.py && \
              rm -rf /tmp/artifacts && \
              yum install -y python-pip && \
              pip install requests && \
              python validate-test-subject.py -s \$(pwd)/${imageName} && \
              exit \$?
              """

    handlePipelineStep() {
        executeInContainer(containerName: container, containerScript: cmd)
    }
}
