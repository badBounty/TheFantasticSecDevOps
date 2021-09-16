import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    def projname = env.JOB_NAME
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-C\C++": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
          sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_Server_IP}:/home"
          sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} pip install flawfinder"
	  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} flawfinder -c -D --csv /home/${projname} > ${projname}-flawfinder.csv"
          /*
          
          Se deberían correr las tools correspondientes luego de copiar el repo remoto a SAST image.
          También, un parser para el output de las tools.
          
          */
        }	
	
        //sh """sed -i -e 's/\\/home\\/${projname}\\///g' nucleiParsedResults.json"""

        /*
        
        Se debe agregar al array de vulns los findings en formato JSON.
        
        */
		    
        notifier.sendMessage('','good','Stage: "SAST-C\C++": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-C\C++": FAILURE')	
        currentBuild.result = 'FAILURE'
	      print('Stage: "SAST-C\C++": FAILURE')
        print(e.getMessage())
    }
}

return this
