def runStage()
{
    def mvnHome = tool name: 'MAVEN-3.6.3', type: 'maven'

    //env.JAVA_HOME ="${tool 'JAVA_HOME_1.8'}"
    env.JAVA_HOME ="${tool 'JAVA_HOME_11'}"

	try
	{
		withEnv(["MVN_HOME=$mvnHome"])
		{
			if (isUnix()) {
	        	sh '"$MVN_HOME/bin/mvn" clean install -X -DskipTests'
	    	} else {
	            bat(/"%MVN_HOME%\bin\mvn" clean install -X -DskipTests/)
	    	}
		}  

	} catch(Exception e)
	{
		notifier.sendMessage('','danger','Stage: "Install-Dependencies": FAILURE')
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Install-Dependencies": FAILURE')
		print(e.printStackTrace())

	}
}
return this