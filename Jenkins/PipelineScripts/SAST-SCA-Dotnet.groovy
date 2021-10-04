import groovy.json.JsonSlurperClassic

def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-SCA-Dotnet": INIT')
		    sh 'find . -name *.sln -exec dotnet list {} package ";"'
        notifier.sendMessage('','good','Stage: "SAST-SCA-Dotnet": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-SCA-Dotnet": FAILURE')	
        currentBuild.result = 'FAILURE'
	      print('Stage: "SAST-SCA-Dotnet": FAILURE')
        print(e.getMessage())
    }
}

return this
