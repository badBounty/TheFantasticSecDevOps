def runStage(notifier)
{
	try
	{
		notifier.sendMessage('','good','Stage: "Install-GitCheckout": INIT')
		git credentialsId: 'git-code-token-manual-clone', branch: "${env.branch}",  url: "${env.Code_Repo_URL}"
                
        GIT_COMMIT_EMAIL = sh (script: 'git show -s --pretty=%an',returnStdout: true).trim()
		GIT_COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
		
		notifier.sendMessage('','good',"Stage: Install-GitCheckout: Git committer --> ${GIT_COMMIT_EMAIL}")
		notifier.sendMessage('','good',"Stage: Install-GitCheckout: Git id --> ${GIT_COMMIT_ID}")

		notifier.sendMessage('','good','Stage: "Install-GitCheckout": SUCCESS')
	} 
	catch(Exception e)
	{
		notifier.sendMessage('','danger','Stage: "Install-GitCheckout": FAILURE')
		//failureMessage = e.getMessage()
		//notifier.sendMessage('','danger','Reason of Failure: ${failureMessage}')
		currentBuild.result = 'FAILURE'
		print('Stage: "Install-GitCheckout": FAILURE')
		print(e.getMessage())
	}
}
return this

