def runStage(){

    def mvnHome = tool name: 'MAVEN-3.6.3', type: 'maven'
    slackSend color: 'good', message: 'Starting Building...'
    slackSend channel: 'notificaciones_cliente', color: 'good', message: 'Starting Building...'

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