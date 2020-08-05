def runStage(port){
    try {
        sshagent(['ssh-key']) {
            def projname = env.JOB_NAME
            sh 'ssh-keygen -f "/var/jenkins_home/.ssh/known_hosts" -R [192.168.0.23]:44022'
            sh "ssh -p 44022 -o StrictHostKeyChecking=no start.sh ${nobuild} ${projname} ${port}"
        }

    }catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the SAST deployment stage' 
        print('------Stage "SAST Deploymeny": FAILURE ------')

    }
}

return this