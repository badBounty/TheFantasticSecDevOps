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
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd)/npmAudit.json root@${env.SAST_Server_IP}:/home"
	  //Correr parser	
        }    
	    
	//Enviar a SAST el json, parsear y agregar a vulns[]. El NPM Audit se realiza en jenkins.
             
	/*
        def results = sh(script: "cat output.json", returnStdout: true).trim()
        def severity = sh(script: "cat severity.txt", returnStdout: true).trim()
        results = results.replace("\\", "")
        results = results.replace("\"", "\\\"")
        results = results.replace("\n", " ")
        if (severity == "Critical"){
            severity = "High"
        }
        vulns.add(["Outdated 3rd Party libraries", results, projname, 0, projname, "null", severity, "NPM-Audit"])
        */
	    
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
