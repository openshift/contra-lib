import org.centos.contra.pipeline.GitUtils


def call(String credentialsId) {

    def gitutils = new GitUtils()

    gitutils.connect(credentialsId)

}