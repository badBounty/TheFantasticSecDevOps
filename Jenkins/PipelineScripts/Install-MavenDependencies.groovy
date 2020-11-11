def runStage(notifier)
{
	notifier.sendMessage('','danger','Stage: "Install-Dependencies": INIT')

    def mvnHome = tool name: 'MAVEN-3.6.3', type: 'maven'

    //env.JAVA_HOME ="${tool 'JAVA_HOME_1.8'}"
    env.JAVA_HOME ="${tool 'JAVA_HOME_11'}"

	try
	{
		withEnv(["MVN_HOME=$mvnHome"])
		{
			
	        sh '"$MVN_HOME/bin/mvn" clean install -X -DskipTests'
	    	
		}  

		notifier.sendMessage('','danger','Stage: "Install-Dependencies": SUCESS')

	} catch(Exception e)
	{
		notifier.sendMessage('','danger','Stage: "Install-Dependencies": FAILURE')
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Install-Dependencies": FAILURE')
		print(e.printStackTrace())

	}
}
return this