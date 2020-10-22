def runStage()
{
	try 
	{
		sh 'npm install'
	} 
	catch(Exception e)
	{
		//TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "Install-GitCheckout": FAILURE'
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Install-GitCheckout": FAILURE')
		print(e.printStackTrace())
	} 
}
return this