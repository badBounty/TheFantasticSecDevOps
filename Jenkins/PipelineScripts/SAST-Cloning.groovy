def runStage(notifier)
{
    def projname = env.JOB_NAME
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Cloning": INIT')
	
        sshagent(['ssh-key-SAST-image']) 
        {
          sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
    	  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm -rf /home/${projname}"
          sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_Server_IP}:/home"
        }	
	
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Cloning": FAILURE')	
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-Cloning": FAILURE')
        print(e.getMessage())
    }
}

return this