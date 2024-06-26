def runStage(notifier)
{	
	try 
	{
		notifier.sendMessage('','good','Stage: "Install-Dependencies": INIT')
		
		//NPM CLI LOGIN Disabled. It asks for OTP.
		/*
		withCredentials([usernamePassword(credentialsId: 'git-code-token-nodeJS', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
		{
			sh """npm-cli-login -u ${USERNAME} -p ${PASSWORD} -e ${env.EmailPrivateRepo}"""
		}
		*/
		
		sh """ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts"""
		sh 'npm install --force --legacy-peer-deps'
		
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
