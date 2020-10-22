def runStage()
{
    try 
    {
        sh 'find . -name *.sln -exec dotnet build {} ";"'
    }
    catch(Exception e)
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "Build-Dotnet": FAILURE'
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Build-Dotnet": FAILURE')
		print(e.printStackTrace())
    }
}
return this