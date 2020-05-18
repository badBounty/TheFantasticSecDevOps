def runStage(){

    def dockerUser = 'mojedalopez'
    def appname = 'webgoat'
    slackSend color: 'good', message: 'Starting Deploy...'

    try {

        def dockerRun = "docker run -p 8080:8080 -d --name ${appname} ${dockerUser}/${appname}"

        sshagent(['ssh-deploy']) {
            sh "ssh -o StrictHostKeyChecking=no dtt@172.16.222.49 ${dockerRun} "
        }
        
        slackSend color: 'good', message: 'The application is running: http://172.16.222.49:8080'
        print('------Stage "Deploy": SUCCESS ------')

    } catch(Exception e) {
         try {

            def dockerRun = "/home/dtt/runDocker.sh ${appname} ${dockerUser}"

            sshagent(['ssh-deploy']) {
                sh "ssh -o StrictHostKeyChecking=no dtt@172.16.222.49 ${dockerRun} "
            }//docker stop name && docker rm -f name && docker image rm user/name

            slackSend color: 'good', message: 'The application is running: http://172.16.222.49:8080'
            print('------Stage "Deploy": SUCCESS ------')

        } catch(Exception d) {

            currentBuild.result = 'FAILURE'    
            print('------Stage "Deploy": FAILURE ------')  
        } // try-catch-finally 
    } // try-catch-finally 
} 

return this