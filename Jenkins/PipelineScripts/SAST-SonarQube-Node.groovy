def runStage(){

    try {
        def projname = env.JOB_NAME
        
        sshagent(['ssh-key']) {
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} /home/sonarscanner/bin/sonar-scanner -Dsonar.projectKey=${projname} -Dsonar.projectBaseDir=/home/${projname} -Dsonar.host.url=http://localhost:9000"
        }
        
        slackSend color: 'good', message: 'SonarQube analysis: SUCCESS' 
        print('------Stage "SonarQube analysis": SUCCESS ------')
    } catch(Exception e) {
        
        currentBuild.result = 'FAILURE'
        slackSend color: 'danger', message: 'An error occurred in the "SonarQube analysis" stage' 
        print('------Stage "SonarQube analysis": FAILURE ------')
    } // try-catch-finally
}

return this