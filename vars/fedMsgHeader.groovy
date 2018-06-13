def call(Map parameters = [:]) {

    return {
        def defaultBuildUrl = "${env.JENKINS_URL}blue/organizations/jenkins/${env.JOB_NAME}/detail/${env.JOB_NAME}/${env.BUILD_NUMBER}/pipeline/"

        return [

                branch           : parameters.get('branch') ?: 'N/A',
                build_id         : parameters.get('buildId') ?: env.BUILD_ID,
                build_url        : parameters.get('buildurl') ?: defaultBuildUrl,
                namespace        : parameters.get('namespace') ?: 'N/A',
                nvr              : parameters.get('nvr') ?: 'N/A',
                original_spec_nvr: parameters.get('originalSpecNvr') ?: 'N/A',
                ref              : parameters.get('ref') ?: 'N/A',
                repo             : parameters.get('repo') ?: 'N/A',
                rev              : parameters.get('rev') ?: 'N/A',
                status           : parameters.get('result') ?: currentBuild.getResult(),
                test_guidance    : "''",
                topic            : parameters.get('topic') ?: 'N/A',
                username         : parameters.get('username') ?: 'N/A'
        ]
    }
}
