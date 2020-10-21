def runStage(){

    slackSend color: 'good', message: 'Starting Building...'
    slackSend channel: 'notificaciones_cliente', color: 'good', message: 'Starting Building...'

    try {
        sh 'npm build'

        slackSend color: 'good', message: 'Maven Build: SUCCESS ' 
        print('------Stage "Build": SUCCESS ------')

    } catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "Maven Build" stage' 
        print('------Stage "Build": FAILURE ------')
    } // try-catch-finally   
}

return this