import org.centos.contra.pipeline.GitUtils


def call(String tag, String msg, String username, String password, String repo) {
    def gitutils = new GitUtils(username, password, repo)

    gitutils.createRelease(tag, msg)

    gitutils.getReleaseByTagName(tag)

    gitutils.getLatestRelease()


}