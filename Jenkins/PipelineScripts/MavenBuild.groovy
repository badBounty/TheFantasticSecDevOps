def runStage(){

    slackSend color: 'good', message: 'Starting Building...'
    slackSend channel: 'general', color: 'good', message: 'Starting Building...'

    try {
        withEnv(["MVN_HOME=$mvnHome"]) {

            if (isUnix()) {
                sh '"$MVN_HOME/bin/mvn" package -X -DskipTests'
            } else {
                bat(/"%MVN_HOME%\bin\mvn" package -X -DskipTests/)
            }
        }  

        slackSend color: 'good', message: 'Maven Build: SUCCESS ' 
        print('------Stage "Maven Build": SUCCESS ------')

    } catch(Exception e) {
        
        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "Maven Build" stage' 
        print('------Stage "Maven Build": FAILURE ------')
    } // try-catch-finally   
}

return this