def runStage(notifier, vulns)
{	
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-NPMAudit": INIT')

        def projname = env.JOB_NAME
	    
	def resultsNPMAudit = sh(script: "npm audit --json",returnStdout: true) 
	writeFile(file: 'npmAudit.json', text: resultsNPMAudit)
	print(resultsNPMAudit)
	    
	sshagent(['ssh-key-SAST-image']) 
        {
          sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd)/npmAudit.json root@${env.SAST_Server_IP}:/home/npmAudit.json"
          sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseNPMAuditResults.py /home/npmAudit.json /home/npmAuditParsed.json"
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/npmAuditParsed.json ./NPMAuditParsed.json"	
        }    
	   
	def results = sh(script: "cat ./NPMAuditParsed.json", returnStdout: true).trim()
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
		vulns.add([title, message, component, line, affected_code, hash, sev, "NPM-Audit"])
	    }
        }    
	    
        notifier.sendMessage('','good','Stage: "SAST-NPMAudit": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-NPMAudit": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-NPMAudit": FAILURE')
        print(e.getMessage())
    }
}
return this
