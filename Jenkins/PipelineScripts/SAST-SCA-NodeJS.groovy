import groovy.json.JsonSlurperClassic

def runStage(notifier)
{
    def projname = env.JOB_NAME
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-SCA-NodeJS": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
          sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
          sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/scaNodeJS.py /home/${projname} /home/scaNodeJS-${projname}.json ${projname}"
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/scaNodeJS-${projname}.json ./scaNodeJS-${projname}.json"
          sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/scaNodeJS-${projname}.json"	
        }	

        def results = sh(script: "cat ./scaNodeJS-${projname}.json | python -m json.tool", returnStdout: true)
        print(results)
        sh(script: "rm ./scaNodeJS-${projname}.json")
		    
        notifier.sendMessage('','good','Stage: "SAST-SCA-NodeJS": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-SCA-NodeJS": FAILURE')	
        currentBuild.result = 'FAILURE'
	      print('Stage: "SAST-SCA-NodeJS": FAILURE')
        print(e.getMessage())
    }
}

return this
