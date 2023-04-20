import groovy.json.JsonSlurperClassic

def runStage() {
    try {
        sshagent(['ssh-key-SAST-image']) {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_SERVER_IP}]:${env.SAST_SERVER_SSH_PORT}"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} cd /home"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} mv /opt/sonarqube/insider /home"
	        sh(script: "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} /home/insider --tech ${env.INSIDER_TECHNOLOGY} --target /home/${env.REPO_TO_SCAN_NAME}",returnStatus: true) 
	    }	  
        print('Stage: "SAST-Insider": SUCCESS')
    }
    catch(Exception e) {
        currentBuild.result = 'FAILURE'
	    print('Stage: "SAST-Insider": FAILURE')
        print(e.getMessage())
    }
}
return this
