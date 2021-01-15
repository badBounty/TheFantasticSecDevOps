def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "DAST-Deployment": INIT')

        sshagent(['ssh-key-SAST-server']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} docker run -d --name ${projname} dast-image -p ${env.DASTport}"
            sh 'sleep 10m'
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} docker container ls -a"
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