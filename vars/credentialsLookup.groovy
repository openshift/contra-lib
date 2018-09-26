import org.centos.contra.pipeline.Utils
import org.centos.contra.pipeline.GitHub


def call() {

    def gitHub = new GitHub()

    gitHub.connect()

}