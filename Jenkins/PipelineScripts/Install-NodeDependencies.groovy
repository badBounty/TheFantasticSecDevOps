def runStage(){

	slackSend color: 'good', message: 'Installing dependencies...'

	try {

		sh 'npm install'

		slackSend color: 'good', message: 'Node install dependencies: SUCCESS'
		slackSend channel: 'notificaciones_cliente', color: 'good', message: 'Dependency installation was successful. Starting SAST...'

		print('------Stage "npm install": SUCCESS ------')

	} catch(Exception e) {

		currentBuild.result = 'FAILURE'    
		slackSend color: 'danger', message: 'An error occurred in the "Install Dependencies" stage' 	
		print('------Stage "npm install": FAILURE ------')

	} // try-catch-finally
}

return this