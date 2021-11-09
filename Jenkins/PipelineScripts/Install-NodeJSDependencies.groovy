def runStage(notifier)
{	
	try 
	{
		def emailPrivateRepo = env.EmailPrivateRepo
		notifier.sendMessage('','good','Stage: "Install-Dependencies": INIT')
		
		withCredentials([usernamePassword(credentialsId: 'git-code-token-nodeJS', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
		{
			sh 'npm-cli-login -u "${USERNAME}" -p "${PASSWORD}" -e ${env.EmailPrivateRepo}'
		}
		
		//sh 'npm install --force'
		
		notifier.sendMessage('','good','Stage: "Install-Dependencies": SUCCESS')
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
