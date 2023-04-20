import groovy.json.JsonSlurperClassic

def runStage() {
    try {
        sshagent(['ssh-key-SAST-image']) {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_SERVER_IP}]:${env.SAST_SERVER_SSH_PORT}"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} python3 /home/scaNodeJS.py /home/${env.REPO_TO_SCAN_NAME} /home/scaNodeJS-${env.REPO_TO_SCAN_NAME}.json ${env.REPO_TO_SCAN_NAME}"
            sh "scp -P ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP}:/home/scaNodeJS-${env.REPO_TO_SCAN_NAME}.json ./scaNodeJS-${env.REPO_TO_SCAN_NAME}.json"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} rm /home/scaNodeJS-${env.REPO_TO_SCAN_NAME}.json"	
        }	
        def results = sh(script: "cat ./scaNodeJS-${env.REPO_TO_SCAN_NAME}.json | python -m json.tool", returnStdout: true)
        print(results)
        sh(script: "rm ./scaNodeJS-${env.REPO_TO_SCAN_NAME}.json")
        print('Stage: "SAST-SCA-NodeJS": SUCCESS')
    }
    catch(Exception e) {	
        currentBuild.result = 'FAILURE'
	    print('Stage: "SAST-SCA-NodeJS": FAILURE')
        print(e.getMessage())
    }
}
return this
