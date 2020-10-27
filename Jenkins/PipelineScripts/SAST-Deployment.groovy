notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}


def runStage()
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Deployment": INIT')

        sshagent(['ssh-key-vm']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} screen -d -m ${env.repositoryFolder}/start.sh nobuild ${projname} ${env.sonarport} ${env.port}"
            sh 'sleep 15m'
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} docker container ls -a"
        }

        notifier.sendMessage('','good','Stage: "SAST-Deployment": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Deployment": FAILURE')	
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Deployment": FAILURE')
        print(e.printStackTrace())
    }
}
return this