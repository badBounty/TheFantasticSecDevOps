def runStage(){

    def dockerHubCred -'docker-hub'

    try {

        docker.withRegistry('', "{dockerHubCred}"){dockerImage.push()}
        slackSend color: 'good', message: 'Push Image to Docker-Hub: SUCCESS ' 
        print('------Stage "Push Image to Docker-Hub": SUCCESS ------')

    } catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "Push Image to Docker-Hub" stage' 
        print('--------Stage "Push Image to Docker-Hub": FAILURE --------')
    } // try-catch-finally 
} 

return this