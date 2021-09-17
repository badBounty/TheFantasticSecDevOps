import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    def projname = env.JOB_NAME
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-C-CPP": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
          sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_Server_IP}:/home"
	  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} flawfinder -c -D --csv /home/${projname} >> flawfinder.csv"
	  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseFlawfinderResults.py /home/flawfinder.csv /home/flawfinder-results-parsed.json ${projname}"
          sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/flawfinder.csv"	
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/flawfinder-results-parsed.json ./flawfinderParsedResults.json"
        }	

        def results = sh(script: "cat ./flawfinderParsedResults.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)
        results = null
        	
        json.each{issue ->
            def title = issue["title"]
            def message = issue["description"]
	    def component = issue["component"]
            def sev = issue["severity"]
	    def line = issue["line"]
	    def affected_code = issue["affectedCode"]
	    def hash = sh(returnStdout: true, script: "sha256sum \$(pwd)/${component} | awk 'NR==1{print \$1}'")    
            hash = hash.replace("\n", " ")
	    if (title.matches("[a-zA-Z0-9].*")){
		vulns.add([title, message, component, line, affected_code, hash, sev, "Flawfinder"])
	    }
        }
		    
        notifier.sendMessage('','good','Stage: "SAST-C-CPP": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-C-CPP": FAILURE')	
        currentBuild.result = 'FAILURE'
	      print('Stage: "SAST-C-CPP": FAILURE')
        print(e.getMessage())
    }
}

return this
