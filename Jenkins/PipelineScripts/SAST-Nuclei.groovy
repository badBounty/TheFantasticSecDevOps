import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    def projname = env.JOB_NAME
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Nuclei": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
	    sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
	    sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_Server_IP}:/home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd /home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mv /opt/sonarqube/nuclei /home"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/nuclei -ut"
		
	    /*
	    //Primera copia de Nuclei Custom Templates
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cp -a /home/Nuclei-Custom-Templates/. /root/nuclei-templates/file"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mkdir TheFantasticDevSecOps"
	    withCredentials([usernamePassword(credentialsId: 'git-code-token-clone', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
		{
		  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} git clone https://${USERNAME}:${PASSWORD}@github.com/badBounty/TheFantasticSecDevOps.git /home/TheFantasticDevSecOps" 
		}
	    //Segunda copia de Nuclei Custom Templates en caso de que se haya agregado uno en el momento del Pipeline
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cp -rf /home/TheFantasticDevSecOps/SAST/Nuclei-Custom-Templates/* /root/nuclei-templates/file"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/TheFantasticDevSecOps/ -r"
            */    
		
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/nuclei -t /root/nuclei-templates/file/ -target /home/${projname} -exclude-tags ${env.nucleiTagsExclusion} -o /home/nuclei-results.txt -json"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseNucleiResults.py /home/nuclei-results.txt /home/nuclei-results-parsed.json ${projname}"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/nuclei-results.txt"	
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/nuclei-results-parsed.json ./nucleiParsedResults.json"
        }	
	
	//sh """sed -i -e 's/\\/home\\/${projname}\\///g' nucleiParsedResults.json"""
       	
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
		    
        notifier.sendMessage('','good','Stage: "SAST-Nuclei": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Nuclei": FAILURE')	
        currentBuild.result = 'FAILURE'
	print('Stage: "SAST-Nuclei": FAILURE')
        print(e.getMessage())
    }
}

return this
