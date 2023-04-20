import groovy.json.JsonSlurperClassic

def runStage() {
    try {
        sshagent(['ssh-key-SAST-image']) {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_SERVER_IP}]:${env.SAST_SERVER_SSH_PORT}"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} cd /home"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} git clone https://github.com/returntocorp/semgrep-rules"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} mv semgrep-rules /home"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} semgrep --config /home/semgrep-rules/${env.SEMGREP_RULE}/ --config /home/semgrep-rules/generic/secrets/security/ /home/${env.REPO_TO_SCAN_NAME}/ -o /home/semgrep${env.REPO_TO_SCAN_NAME}.json --json --skip-unknown-extensions"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} python3 /home/SemgrepParser.py /home/semgrep${env.REPO_TO_SCAN_NAME}.json /home/semgrepParsed.json ${env.REPO_TO_SCAN_NAME}"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} rm /home/semgrep${env.REPO_TO_SCAN_NAME}.json"	
            sh "scp -P ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP}:/home/semgrepParsed.json ./semgrepParsedResults.json"     
        }	
        def results = sh(script: "cat ./semgrepParsedResults.json", returnStdout: true).trim()
	    def json = new JsonSlurperClassic().parseText(results)
        results = null
        json.each{issue ->
            def title = issue["title"]
            def message = issue["title"]
	        def component = issue["component"]
            def sev = issue["severity"]
	        def line = issue["line"]
	        def affected_code = issue["affectedCode"]
	        def hash = sh(returnStdout: true, script: "sha256sum \$(pwd)/${component} | awk 'NR==1{print \$1}'")    
            hash = hash.replace("\n", " ")
	        if (title.matches("[a-zA-Z0-9].*")) {
		        vulns.add([title, message, component, line, affected_code, hash, sev, "Semgrep"])
	        }
        }
        print('Stage: "SAST-Semgrep": SUCCESS')
    }
    catch(Exception e) {	
        currentBuild.result = 'FAILURE'
      	print('Stage: "SAST-Semgrep": FAILURE')
        print(e.getMessage())
    }
}
return this
