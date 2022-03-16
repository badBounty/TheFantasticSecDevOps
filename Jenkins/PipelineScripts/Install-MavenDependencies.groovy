def runStage(notifier)
{
	notifier.sendMessage('','good','Stage: "Install-Dependencies": INIT')
	try
	{
		sh 'mvn clean install -X -DskipTests'
		notifier.sendMessage('','good','Stage: "Install-Dependencies": SUCESS')
	} 
	catch(Exception e)
	{
		notifier.sendMessage('','danger','Stage: "Install-Dependencies": FAILURE')
		currentBuild.result = 'FAILURE'
		print('Stage: "Install-Dependencies": FAILURE')
		print(e.printStackTrace())
	}
}
return this
