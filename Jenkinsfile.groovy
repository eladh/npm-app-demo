server = Artifactory.server "artifactory"
rtFullUrl = server.url
rtIpAddress = rtFullUrl - ~/^http?.:\/\// - ~/\/artifactory$/


podTemplate(label: 'jenkins-pipeline' , cloud: 'k8s' , containers: [
        containerTemplate(name: 'node', image: 'node:8', command: 'cat', ttyEnabled: true)]) {

    node('jenkins-pipeline') {

        def buildNumber = env.BUILD_NUMBER
        def branchName = env.BRANCH_NAME
        def workspace = env.WORKSPACE
        def buildUrl = env.BUILD_URL

        stage ('Clone') {
            git url: 'https://github.com/eladh/npm-app-demo.git' ,credentialsId: 'github'
        }


        stage('Info') {
            echo "workspace directory is $workspace"
            echo "build URL is $buildUrl"
            echo "build Number is $buildNumber"
            echo "branch name is $branchName"
            echo "PATH is $env.PATH"
        }

        stage ('Prep env') {
            container('node') {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactorypass',
                                  usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                    sh("curl -u${env.USERNAME}:${env.PASSWORD} http://${rtIpAddress}:80/artifactory/api/npm/auth > ~/.npmrc")
                    sh('echo "email = youremail@email.com" >> ~/.npmrc')
                    sh("npm config set registry http://${rtIpAddress}/artifactory/api/npm/npm-virtual/")
                }
            }
        }

        stage ('Install npm') {
            container('node') {
                sh 'npm install'
            }
        }

        stage ('Build npm') {
            container('node') {
                sh 'npm run build'
                sh 'cp package.json dist/'
            }
        }

        stage ('Publish npm') {
            container('node') {
//
                sh 'npm version minor'
                sshagent(['git-ssh-key']) {
                    sh "git commit -m 'new minor update' package.json; git push origin master;"
                }

                sh "npm publish --tag next"
            }
        }
    }
}