def call(Map parameters = [:]) {
    def ciMessage = readJSON text: parameters.get('ciMessage', env.CI_MESSAGE)
    def imagePrefix = parameters.get('imagePrefix', 'Fedora')

    def compose = ciMessage['compose_id']
    def release_version = ciMessage['release_version']
    def location = ciMessage['location']

    currentBuild.displayName = "${release_version} - ${compose}"

    def imageName = "${imagePrefix}-${release_version}.qcow2"

    handlePipelineStep() {

        sh """
       rm -f images.json 
       curl -LO ${location}/metadata/images.json
    """

        def imageJson = readJSON file: 'images.json'
        def images = imageJson['payload']['images']
        def imagePath = null

        if (images['Cloud']) {
            if (images['Cloud']['x86_64']) {
                images['Cloud']['x86_64'].each { build ->
                    if (build['format'] == 'qcow2') {
                        imagePath = build['path']
                    }
                }
            } else {
                error("There are no x86_64 images available")
            }
        } else {
            error("There are no cloud images available")
        }

        if (!imagePath) {
            error("There are no qcow2 images available")
        }

        sh("curl -Lo ${imageName} ${location}/${imagePath}")

    }

    return imageName

}
