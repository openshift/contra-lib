import org.centos.contra.pipeline.Utils
import org.centos.contra.pipeline.GitUtils


def call() {

    def gitutils = new GitUtils()

    gitutils.connect()

}