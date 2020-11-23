def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-SendVulnsLog": INIT')

        sshagent(['ssh-key-SAST-server']) 
        {
            def logs = sh(script: "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} cat ${env.SAST_Server_Repository_SAST_Path}/titleNormalization.log", returnStdout: true).trim()
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} rm titleNormalization.log"
            notifier.sendMessage('','good',"Stage: SAST-SendVulnsLog: ${logs}")
        }

        notifier.sendMessage('','good','Stage: "SAST-SendVulnsLog": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-SendVulnsLog": FAILURE')	
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Deployment": FAILURE')
        print(e.printStackTrace())
    }
}

return this