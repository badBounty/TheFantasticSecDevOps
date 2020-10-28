/*notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}
*/
def runStage()
{
	try 
	{
		sh 'find . -name *.sln -exec dotnet restore {} ";"'
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