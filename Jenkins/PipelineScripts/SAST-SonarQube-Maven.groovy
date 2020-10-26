notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}

def runStage()
{

	def mvnHome = tool name: 'MAVEN-3.6.3', type: 'maven'

    try {

        withEnv(["MVN_HOME=$mvnHome"])
        {
			if (isUnix()) 
            {
                sh "'$MVN_HOME/bin/mvn' sonar:sonar -Dsonar.host.url=http://${env.SASTIP}:${env.sonarport} -Dsonar.login=${env.sonartoken} -X -DskipTests "
            } 
            else 
            {
                bat(/"%MVN_HOME%\bin\mvn" sonar:sonar -X -DskipTests/)
            }
        }

    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-SonarQube": FAILURE')

        currentBuild.result = 'FAILURE'
        print('Stage "SAST-SonarQube": FAILURE')
        print(e.printStackTrace())
    }
}

return this