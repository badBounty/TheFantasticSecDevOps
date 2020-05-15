def runStage(){

	slackSend color: 'good', message: 'Starting SAST...'

    try {

        withEnv(["MVN_HOME=$mvnHome"]) {
			if (isUnix()) {
                sh '"$MVN_HOME/bin/mvn" sonar:sonar -Dsonar.host.url=http://172.16.222.50:9000 -Dsonar.login=84ab8ef32309d4d8318db0f6a1743f9a147378a2 -X -DskipTests ' 
            } else {
                    bat(/"%MVN_HOME%\bin\mvn" sonar:sonar -X -DskipTests/)
            }
        }  

        slackSend color: 'good', message: 'SonarQube analysis: SUCCESS' 
        print('------Stage "SonarQube analysis": SUCCESS ------')

    } catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "SonarQube analysis" stage' 
        print('------Stage "SonarQube analysis": FAILURE ------')

    } // try-catch-finally
}

return this