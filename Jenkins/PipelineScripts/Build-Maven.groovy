def runStage()
{
    def mvnHome = tool name: 'MAVEN-3.6.3', type: 'maven'
    try
    {
        withEnv(["MVN_HOME=$mvnHome"]) {

            if (isUnix()) {
                sh '"$MVN_HOME/bin/mvn" package -X -DskipTests'
            } else {
                bat(/"%MVN_HOME%\bin\mvn" package -X -DskipTests/)
            }
        }  
    }
    catch(Exception e)
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "Build-Maven": FAILURE'
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Build-Maven": FAILURE')
		print(e.printStackTrace())
    }
}
return this