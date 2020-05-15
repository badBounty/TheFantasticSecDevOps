def runStage(){

    // Maven Version
    def mvnHome = tool name: 'MAVEN-3.6.3', type: 'maven'

    //Java Version
    //env.JAVA_HOME ="${tool 'JAVA_HOME_1.8'}"
    env.JAVA_HOME ="${tool 'JAVA_HOME_11'}"

	slackSend color: 'good', message: 'Installing dependencies...'

	try {
		withEnv(["MVN_HOME=$mvnHome"]) {
			if (isUnix()) {
	        	sh '"$MVN_HOME/bin/mvn" clean install -X -DskipTests'
	    	} else {
	            bat(/"%MVN_HOME%\bin\mvn" clean install -X -DskipTests/)
	    	}
		}  

	slackSend color: 'good', message: 'Maven install: SUCCESS'
	slackSend channel: 'general', color: 'good', message: 'Dependency installation was successful. Starting SAST...'

	print('------Stage "Maven install": SUCCESS ------')

	} catch(Exception e) {

		currentBuild.result = 'FAILURE'    
		slackSend color: 'danger', message: 'An error occurred in the "Maven install" stage' 	
		print('------Stage "Maven install": FAILURE ------')

	} // try-catch-finally
}

return this