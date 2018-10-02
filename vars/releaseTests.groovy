import org.centos.contra.pipeline.GitUtils


def call(String tag, String msg, String username, String password, String repo) {
    def gitutils = new GitUtils(username: username, password: password, repo: repo)

    print gitutils.createRelease(tag, msg)

    print gitutils.getLatestRelease()

   // gitutils.getReleaseByTagName(tag, repo)



}
