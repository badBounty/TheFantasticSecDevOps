def runStage() {
    try {
        sshagent(['ssh-key-SAST-server']) {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} screen -d -m ${env.SAST_Server_Repository_SAST_Path}/start.sh nobuild ${projname} ${env.Sonar_Port} ${env.SAST_Server_SSH_Port}"
            sh 'sleep 1m'
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} docker container ls -a"
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} ls -a -l"
        }
    }
    catch(Exception e) {	      
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Deployment": FAILURE')
        print(e.printStackTrace())
    }
}
return this
