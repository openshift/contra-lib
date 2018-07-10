def call(Map parameters = [:]) {
    def imageName = parameters.get('imageName')

    def cmd = """
              curl -O https://pagure.io/upstream-fedora-ci/raw/master/f/fedora-ci-monitor/validate-test-subject.py && \
              rm -rf /tmp/artifacts && \
              yum install -y python-pip && \
              pip install requests && \
              python validate-test-subject.py -s \$(pwd)/${imageName} && \
              exit \$?
              """

    handlePipelineStep() {
        executeInContainer(containerName: 'singlehost-test', containerScript: cmd)
    }
}
