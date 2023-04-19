def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Destroy": INIT')

        sshagent(['ssh-key-SAST-server']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} docker container rm -v -f ${projname}"
        }
        
        notifier.sendMessage('','good','Stage: "SAST-Destroy": SUCCESS')
    }
    catch(Exception e) 
    {
		notifier.sendMessage('','danger','Stage: "SAST-Destroy": FAILURE')
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Destroy": FAILURE')
        print(e.printStackTrace())
    }
}
return this
