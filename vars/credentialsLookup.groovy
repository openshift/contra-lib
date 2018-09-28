import org.centos.contra.pipeline.GitUtils


def call(String credentialsId) {

    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {

        def gitutils = new GitUtils(env.USERNAME, env.PASSWORD)

        gitutils.mergePR('joejstuart/dockerImages', 1, "merging with api")
    }

}