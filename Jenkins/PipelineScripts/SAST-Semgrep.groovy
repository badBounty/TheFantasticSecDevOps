import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    def projname = env.JOB_NAME
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Semgrep": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
          	
	     //SIEMPRE ACTUALIZAR SEMGREP. Algunas rules no se pueden parsear si no está en la ultima version.
            //Clonar registry rules de semgrep.
            //Ver la forma de whitelistear o blacklistear.
            //Correr semgrep.
            //Guardar en un output y parsear.
            
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd /home"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mv /opt/sonarqube/nuclei /home"
            
        }	
	
        /*

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
		    */
      
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
