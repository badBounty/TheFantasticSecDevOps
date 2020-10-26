def runStage()
{
    try 
    {
        sshagent(['ssh-key-vm']) 
        {
            sh "ssh -o StrictHostKeyChecking=no ${env.SASTVMUSER}@${env.SASTIP} docker container rm -f ${env.JOB_NAME}"
        }
    }
    catch(Exception e) 
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "SAST-Deployment": FAILURE'
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Deployment": FAILURE')
        print(e.printStackTrace())
    }
}
return this