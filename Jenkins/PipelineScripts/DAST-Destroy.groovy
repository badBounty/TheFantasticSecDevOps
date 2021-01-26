def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "DAST-Destroy": INIT')

        sshagent(['ssh-key-SAST-server']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.DAST_Server_User}@${env.DAST_Server_IP} docker container rm -f ${projname}"
        }
        
        notifier.sendMessage('','good','Stage: "DAST-Destroy": SUCCESS')
    }
    catch(Exception e) 
    {
		notifier.sendMessage('','danger','Stage: "DAST-Destroy": FAILURE')
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Destroy": FAILURE')
        print(e.printStackTrace())
    }
}
return this