import groovy.json.JsonSlurperClassic

def runStage() {
    try {
        withCredentials([usernamePassword(credentialsId: 'sonar-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {       
            sshagent(['ssh-key-SAST-image']) {
                sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} python3 /home/SonarResults.py ${USERNAME} ${PASSWORD} localhost ${env.SONAR_PORT} /home/SonarResultsParsed.json"
		        sh "scp -P ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP}:/home/SonarResultsParsed.json ./SonarResultsParsed.json"    
            }
        }
	    def results = sh(script: "cat ./SonarResultsParsed.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)
        results = null  
	    
        json.each {issue ->
            def title = issue["title"]
            def message = issue["title"]
	        def component = issue["component"]
            def sev = issue["severity"]
	        def line = issue["line"]
	        def affected_code = issue["affectedCode"]
	        def hash = sh(returnStdout: true, script: "sha256sum \$(pwd)/${component} | awk 'NR==1{print \$1}'")    
            hash = hash.replace("\n", " ")
	        if (title.matches("[a-zA-Z0-9].*")) {
		        vulns.add([title, message, component, line, affected_code, hash, sev, "SonarQube"])
	        }
        }
        print('Stage: "SAST-SonarResults": SUCCESS')
    }
    catch(Exception e) {
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-SonarResults": FAILURE')
		print(e.getMessage())
	}
}

return this
