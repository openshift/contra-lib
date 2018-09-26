import org.centos.contra.pipeline.Utils


def call() {

    def utils = new Utils()

    return utils.getCredentialsById('contra-sample-project-docker-credentials', 'password')
}