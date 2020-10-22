def runStage()
{
    try 
    {
        //def dockerUser = 'mojedalopez'
        //def appName = 'webgoat'
        //dockerImage = docker.build("${dockerUser}/${appName}", "./webgoat-server")
    } 
    catch(Exception e)
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "DockerBuild": FAILURE'
		
		currentBuild.result = 'FAILURE'
		print('Stage: "DockerBuild": FAILURE')
		print(e.printStackTrace())
    }
}
return this