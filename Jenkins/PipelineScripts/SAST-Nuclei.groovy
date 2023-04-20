import groovy.json.JsonSlurperClassic

def runStage() {
    try {
        sshagent(['ssh-key-SAST-image']) {
			sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_SERVER_IP}]:${env.SAST_SERVER_SSH_PORT}"
			sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} cd /home"
			sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} mv /opt/sonarqube/nuclei /home"
			sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} /home/nuclei -ut"
			sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} cp -a /home/Nuclei-Custom-Templates/. /root/nuclei-templates/file"   
			sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} /home/nuclei -t /root/nuclei-templates/file/ -target /home/${env.REPO_TO_SCAN_NAME} -exclude-tags ${env.NUCLEI_TAGS_EXCLUSION} -o /home/nuclei-results.txt -json"
			sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} python3 /home/parseNucleiResults.py /home/nuclei-results.txt /home/nuclei-results-parsed.json ${env.REPO_TO_SCAN_NAME}"
			sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} rm /home/nuclei-results.txt"	
			sh "scp -P ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP}:/home/nuclei-results-parsed.json ./nucleiParsedResults.json"
    	}	   	
		def results = sh(script: "cat ./nucleiParsedResults.json", returnStdout: true).trim()
		def json = new JsonSlurperClassic().parseText(results)
		results = null
		json.each{issue ->
			def title = issue["title"]
			def message = issue["title"]
			def component = issue["component"]
			def sev = issue["severity"]
			def line = "N/A"
			def affected_code = issue["affectedCode"]
			def hash = sh(returnStdout: true, script: "sha256sum \$(pwd)/${component} | awk 'NR==1{print \$1}'")    
			hash = hash.replace("\n", " ")
			if (title.matches("[a-zA-Z0-9].*")){
				vulns.add([title, message, component, line, affected_code, hash, sev, "Nuclei"])
			}
		}
    }
    catch(Exception e) {	
        currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Nuclei": FAILURE')
        print(e.getMessage())
    }
}

return this
