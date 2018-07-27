
// which branch/tag to checkout

def branch = 'master'

if (env.TAG_NAME) {
    branch = env.TAG_NAME
} else if (env.BRANCH_NAME) {
    branch = env.BRANCH_NAME
}

libraries = ['contra-lib': [branch, 'https://github.com/openshift/contra-lib.git']]

libraries.each { name, repo ->
    library identifier: "${name}@${repo[0]}",
            retriever: modernSCM([$class: 'GitSCMSource',
                                  remote: repo[1]])

}

node() {
    sh 'env'
}