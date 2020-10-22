def runStage()
{
    try 
    {
        sh 'npm build'
    } 
    catch(Exception e)
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "Build-NodeJS": FAILURE'
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Build-NodeJS": FAILURE')
		print(e.printStackTrace())
    }
}

return this