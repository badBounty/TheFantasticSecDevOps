def runStage() {
    try {
        sshagent(['ssh-key-SAST-image']) {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_SERVER_IP}]:${env.SAST_SERVER_SSH_PORT}"
    	    sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} rm -rf /home/${env.REPO_TO_SCAN_NAME}"
		sh "pwd"
		sh "ls"
	    sh "scp -P ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_SERVER_IP}:/home"
		sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} ls"
        }
        print('Stage: "SAST-Cloning": SUCCESS')	
    }
    catch(Exception e) {	
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-Cloning": FAILURE')
        print(e.getMessage())
    }
}
return this
