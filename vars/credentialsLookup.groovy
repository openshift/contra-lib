import org.centos.contra.pipeline.GitUtils


def call(String credentialsId) {

    def gitutils = new GitUtils(credentialsId)

    gitutils.mergePR('joejstuart/dockerImages', 1, "merging with api")
}