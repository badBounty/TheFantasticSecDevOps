def runStage(){
    try {
        sshagent(['ssh-key-vm']) {
            def projname = env.JOB_NAME
            //sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} screen -d -m ${env.repositoryFolder}/start.sh nobuild ${projname} ${env.sonarport} ${env.port}"
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} ${env.repositoryFolder}/start.sh nobuild ${projname} ${env.sonarport} ${env.port}"
            sh 'sleep 5m'
        }
        print('------Stage "SAST Deploymeny": Success ------')
    }catch(Exception e) {
        print(e.printStackTrace())
        currentBuild.result = 'FAILURE'    
        print('------Stage "SAST Deploymeny": FAILURE ------')
    }
}

return this