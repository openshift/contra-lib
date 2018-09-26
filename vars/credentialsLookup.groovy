import org.centos.contra.pipeline.Utils


def call() {

    def utils = new Utils()

    println utils.getCredentialsById('contra-sample-project-docker-credentials', 'password')
}