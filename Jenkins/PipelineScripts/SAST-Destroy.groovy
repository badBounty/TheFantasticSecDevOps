notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}

def runStage()
{
    try 
    {
        sshagent(['ssh-key-vm']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} docker container rm ${projname}"
        }
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
