def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-SendVulnsLog": INIT')

        sshagent(['ssh-key-vm']) 
        {
            logs = sh(script: "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} cat titleNormalization.log", returnStdout: true).trim()
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} rm titleNormalization.log"
        }

        notifier.sendMessage('','good',"Stage: SAST-SendVulnsLog: ${logs}")

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