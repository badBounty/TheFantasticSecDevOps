import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    def projname = env.JOB_NAME
    def semgrepRule = env.Semgrep_Rule
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Semgrep": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 -m pip install --ignore-installed semgrep" 
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd /home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} git clone https://github.com/returntocorp/semgrep-rules"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mv semgrep-rules /home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} semgrep --config /home/semgrep-rules/${semgrepRule}/ --config /home/semgrep-rules/generic/secrets/security/ /home/${projname}/ -o /home/semgrep${projname}.json --json --skip-unknown-extensions"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/SemgrepParser.py /home/semgrep${projname}.json /home/semgrepParsed.json ${projname}"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/semgrep${projname}.json"	
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/semgrepParsed.json ./semgrepParsedResults.json"
		
	    //SIEMPRE ACTUALIZAR SEMGREP. Algunas rules no se pueden parsear si no está en la ultima version.     
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
	    if (title.matches("[a-zA-Z0-9].*")){
		vulns.add([title, message, component, line, affected_code, hash, sev, "Semgrep"])
	    }
        }
      
        notifier.sendMessage('','good','Stage: "SAST-Semgrep": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Semgrep": FAILURE')	
        currentBuild.result = 'FAILURE'
      	print('Stage: "SAST-Semgrep": FAILURE')
        print(e.getMessage())
    }
}

return this
