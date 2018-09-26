import org.centos.contra.pipeline.Utils


def call() {

    def gitHub = new GitHub()

    gitHub.connect()

}