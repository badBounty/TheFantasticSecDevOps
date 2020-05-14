
def runStage(){
	try {

		git credentialsId: 'gitlab-apitoken', 
		    url: 'https://gitlab.com/NicolasOjedajava/secdevops/'
			
		GIT_COMMIT_EMAIL = sh (
			script: 'git show -s --pretty=%an',
			returnStdout: true
			).trim()

		slackSend channel: 'notificaciones_cliente', color: 'good', message: "New commit detected. Git committer: ${GIT_COMMIT_EMAIL}"	
		slackSend color: 'good', message: "Git committer: ${GIT_COMMIT_EMAIL}"
		slackSend color: 'good', message: 'Git Checkout: SUCCESS'
		print('------Stage "environment config": SUCCESS ------')

	} catch(Exception e) {

		currentBuild.result = 'FAILURE'   
		slackSend channel: 'notificaciones_cliente', color: 'good', message:  'An error occurred in the "Environment config" stage' 	
		slackSend color: 'danger', message: 'An error occurred in the "Environment config" stage' 
		slackSend color: 'danger', message: "Git committer: ${GIT_COMMIT_EMAIL}"
		print('------Stage "environment config": FAILURE ------')

	} // try-catch-finally
}
