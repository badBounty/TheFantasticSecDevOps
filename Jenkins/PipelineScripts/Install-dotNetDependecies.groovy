def runStage()
{
	try 
	{
		sh 'find . -name *.csproj -exec dotnet restore {} ";"'
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