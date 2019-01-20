
    node('generic') {

        def server = Artifactory.server "artifactory"
        def rtNpm = Artifactory.newNpmBuild()
        def buildInfo


        stage ('Clone') {
            git url: 'https://github.com/eladh/demos.git' ,credentialsId: 'github'
        }

        stage ('Artifactory configuration') {
            rtNpm.deployer repo: 'npm-local', server: server
            rtNpm.resolver repo: 'npm-remote', server: server
            buildInfo = Artifactory.newBuildInfo()
        }

        stage ('Install npm') {
            rtNpm.install buildInfo: buildInfo, path: 'client-app' , tool: 'nodejs-tool'
        }

//        stage ('Build npm') {
//            container('node') {
//                sh 'npm i --prefix client-app'
//                sh 'npm run build --prefix client-app'
//                sh 'cp client-app/package.json client-app/dist/'
//            }
//        }
//
//        stage ('Publish npm') {
//            rtNpm.publish buildInfo: buildInfo, path: 'client-app/dist'
//        }
//
//        stage ('Publish build info') {
//            server.publishBuildInfo buildInfo
//        }
    }
