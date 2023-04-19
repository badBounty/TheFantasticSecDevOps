def runStage() {
    try {
        sshagent(['ssh-key-SAST-server']) {
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_SERVER_USERNAME}@${env.SAST_SERVER_IP} screen -d -m ${env.SAST_SERVER_SCRIPTS_SAST_PATH}/start.sh nobuild ${env.REPO_TO_SCAN_NAME} ${env.SONAR_PORT} ${env.SAST_SERVER_SSH_PORT}"
            sh 'sleep 1m'
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_SERVER_USERNAME}@${env.SAST_SERVER_IP} docker container ls -a" //corre si esta vivo el container
            print ('Stage "SAST-Deployment": SUCCESS')
        }
    }
    catch(Exception e) {	      
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Deployment": FAILURE')
        print(e.printStackTrace())
    }
}
return this
