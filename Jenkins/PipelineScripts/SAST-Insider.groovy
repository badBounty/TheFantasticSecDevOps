import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    def projname = env.JOB_NAME
    def insiderTech = env.InsiderTechnology
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Insider": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd /home"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mv /opt/sonarqube/insider /home"
	    def insiderResults = sh(script: "./home/insider --tech ${insiderTech} --target /home/${projname}",returnStdout: true) 
	    writeFile(file: 'insiderResults.json', text: insiderResults)
            //sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/insider --tech ${insiderTech} --target /home/${projname}"
            //sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseInsiderResults.py"
            //sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/nuclei-results.txt"	
            //sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/nuclei-results-parsed.json ./nucleiParsedResults.json"
        }	
	
	/*    
	    
        def results = sh(script: "cat ./insiderParsedResults.json", returnStdout: true).trim()
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
              vulns.add([title, message, component, line, affected_code, hash, sev, "Insider"])
            }
        }
	
	*/
		    
        notifier.sendMessage('','good','Stage: "SAST-Insider": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Insider": FAILURE')	
        currentBuild.result = 'FAILURE'
	      print('Stage: "SAST-Insider": FAILURE')
        print(e.getMessage())
    }
}

return this
