import groovy.json.JsonSlurperClassic

def runStage(notifier)
{
    def projname = env.JOB_NAME
    try 
    {
	notifier.sendMessage('','good','Stage: "SAST-SCA-Java": INIT')
	    
	sshagent(['ssh-key-SAST-image']) 
        {
          sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
          sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/scaJava.py /home/${projname} /home/scaJava-${projname}.json ${projname}"
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/scaJava-${projname}.json ./scaJava-${projname}.json"
          sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/scaJava-${projname}.json"	
        }	

        def results = sh(script: "cat ./scaJava-${projname}.json | python -m json.tool", returnStdout: true)
        print(results)
        sh(script: "rm ./scaJava-${projname}.json")
		    
        notifier.sendMessage('','good','Stage: "SAST-SCA-Java": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-SCA-Java": FAILURE')	
        currentBuild.result = 'FAILURE'
	      print('Stage: "SAST-SCA-Java": FAILURE')
        print(e.getMessage())
    }
}

return this
