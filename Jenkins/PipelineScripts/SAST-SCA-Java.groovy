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
	  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd /home/${projname}"
	  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/scaJava.sh ${projname}"
	  sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/scaMaven-${projname}.txt ./scaMaven-${projname}.txt"
	  //rm scaMaven de SAST.
        }
        
	def results = sh(script: "cat /home/${projname}/scaMaven-${projname}.txt" , returnStdout: true)
        print('Maven Libraries: \n')
        print('Format --> Group:Artifact:Type:Version:Scope \n')
        print(results)
		    
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
