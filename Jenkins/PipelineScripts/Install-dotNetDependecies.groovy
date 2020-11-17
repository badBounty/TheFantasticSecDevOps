def runStage(notifier)
{
	try 
	{

		notifier.sendMessage('','good','Stage: "Install-Dependencies": INIT')
		sh """find . -name \\"*.csproj\\" -exec dotnet restore {} \\\\\\\\\\\\;"""
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