notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}


def runStage()
{
    try 
    {
        notifier.sendMessage('','good','Stage: "Send Vulns Log": INIT')

        sshagent(['ssh-key-vm']) 
        {
            logs = sh(script: "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} cat titleNormalization.log", returnStdout: true).trim()
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} rm titleNormalization.log"
        }

        notifier.sendMessage('','good',"Vulns Log: ${logs}")

        notifier.sendMessage('','good','Stage: "Send Vulns Log": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "Send Vulns Log": FAILURE')	
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Deployment": FAILURE')
        print(e.printStackTrace())
    }
}