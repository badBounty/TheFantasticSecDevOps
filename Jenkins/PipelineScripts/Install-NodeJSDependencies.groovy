def runStage()
{
	try 
	{
		sh 'npm install'
	} 
	catch(Exception e)
	{
		//TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "Install-Dependencies": FAILURE'
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Install-Dependencies": FAILURE')
		print(e.printStackTrace())
	} 
}
return this