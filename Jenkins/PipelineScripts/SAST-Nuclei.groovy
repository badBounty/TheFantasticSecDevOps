def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Nuclei": INIT')

        sshagent(['ssh-key-SAST-image']) 
        {
            def projname = env.JOB_NAME
	    sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_Server_IP}:/home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd /home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mv /opt/sonarqube/nuclei /home"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/nuclei -ut"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cp -a /home/Nuclei-Custom-Templates/. /root/nuclei-templates/file"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mkdir TheFantasticDevSecOps"
	    withCredentials([usernamePassword(credentialsId: 'git-code-token-clone', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
		{
		  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} git clone https://${USERNAME}:${PASSWORD}@github.com/badBounty/TheFantasticSecDevOps.git /home/TheFantasticDevSecOps" 
		}
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cp -rf /home/TheFantasticDevSecOps/SAST/Nuclei-Custom-Templates/* /root/nuclei-templates/file"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/TheFantasticDevSecOps/ -r"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/nuclei -t /root/nuclei-templates/file -target /home/${projname} -o /home/nuclei-results.txt -json"
	    //Correr nucleiParser
	    //scp de nuclei-results parseado.
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/nuclei-results.txt ./nuclei-results.txt"
          
          //Migrar regexScanner.
        }
	    
	/*    
	sh """sed -i -e 's/\\/home\\/${projname}\\///g' nucleiParsedResults.json"""
        
        def results = sh(script: "cat nucleiParsedResults.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)["results"]
        results = null
        
        json.each{issue ->
            def title = issue["title"]
            def message = issue["title"]
	    def component = issue["component"]
            def files = issue["affectedCode"]
            def sev = issue["severity"]
            files.each{file -> 
                def line = "N/A"
                def affected_code = component
                
                affected_code = affected_code.replace("\\", "")
                affected_code = affected_code.replace("\"", "\\\"")
                affected_code = affected_code.replace("\n", " ")
		
                def hash = sh(returnStdout: true, script: "sha256sum \$(pwd)/${component} | awk 'NR==1{print \$1}'")    
                hash = hash.replace("\n", " ")
                if (title.matches("[a-zA-Z0-9].*")){
                    vulns.add([title, message, component, line, affected_code, hash, sev, "Nuclei"])
                }
            }
        }
	*/
	    
        notifier.sendMessage('','good','Stage: "SAST-Nuclei": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Nuclei": FAILURE')	
        currentBuild.result = 'FAILURE'
	print('Stage: "SAST-Nuclei": FAILURE')
        print(e.printStackTrace())
    }
}
return this
