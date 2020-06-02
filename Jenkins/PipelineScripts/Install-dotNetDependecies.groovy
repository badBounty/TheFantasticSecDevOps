def runStage(){

	slackSend color: 'good', message: 'Installing dependencies...'

	try {

 
		sh """find . -name \\"*.sln\\" -exec dotnet restore {} \\\\\\;"""

		slackSend color: 'good', message: 'dotnet restore: SUCCESS'
		slackSend channel: 'notificaciones_cliente', color: 'good', message: 'Dependency installation was successful. Starting SAST...'

		print('------Stage "DotNet core restore": SUCCESS ------')

	} catch(Exception e) {

		currentBuild.result = 'FAILURE'    
		slackSend color: 'danger', message: 'An error occurred in the "dependencies install" stage'
		print('------Stage "DotNet core restore": FAILURE ------')

	} // try-catch-finally
}

return this