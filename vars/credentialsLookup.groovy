import org.centos.contra.pipeline.GitUtils


def call(String credentialsId) {

    withCredentials([usernamePassword(credentialsId: 'contra-sample-project-docker-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {

        def gitutils = new GitUtils(env.USERNAME, env.PASSWORD)

        gitutils.mergePR('joejstuart/dockerImages', 1, "merging with api")
    }

}