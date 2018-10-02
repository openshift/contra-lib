import org.centos.contra.pipeline.GitUtils


def call(String tag, String msg, String username, String password, String repo) {
    def gitutils = new GitUtils(username, password)

    gitutils.createRelease(tag, msg, 'joejstuart/dockerImages')

    gitutils.getReleaseByTagName(tag, 'joejstuart/dockerImages')

    gitutils.getLatestRelease('joejstuart/dockerImages')


}
