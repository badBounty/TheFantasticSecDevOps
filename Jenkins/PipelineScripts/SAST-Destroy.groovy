def runStage(){
    try {
        sshagent(['ssh-key-vm']) {
            def projname = env.JOB_NAME
            sh "ssh-keygen -f "/var/jenkins_home/.ssh/known_hosts" -R ${env.SASTIP}"
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTIP} docker container rm -f ${projname}"
        }

    }catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the SAST deployment stage' 
        print('------Stage "SAST Deploymeny": FAILURE ------')

    }
}

return this