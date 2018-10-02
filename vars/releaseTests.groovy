import org.centos.contra.pipeline.GitUtils


def call(String tag, String msg, String username, String password, String repo) {
    def gitutils = new GitUtils(username: username, password: password)

    print gitutils.createRelease(tag, msg, repo)

    print gitutils.getLatestRelease(repo)

   // gitutils.getReleaseByTagName(tag, repo)



}
