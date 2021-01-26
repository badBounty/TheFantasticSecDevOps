def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "DAST-Deployment": INIT')

        sshagent(['ssh-key-SAST-server']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.DAST_Server_User}@${env.DAST_Server_IP} docker run -d -i --name ${projname} -p ${env.DAST_Server_SSH_Port}:22 secpipeline-dast init.sh \"${env.Authentication}\""
            sh 'sleep 5m'
            sh "ssh -o StrictHostKeyChecking=no ${env.DAST_Server_User}@${env.DAST_Server_IP} docker container ls -a"
        }

        notifier.sendMessage('','good','Stage: "DAST-Deployment": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "DAST-Deployment": FAILURE')	
        
		currentBuild.result = 'FAILURE'
		print('Stage: "DAST-Deployment": FAILURE')
    }
}
return this