def runStage(notifier)
{

    notifier.sendMessage('','good','Stage: "SAST-Sonarqube": INIT')

	def mvnHome = tool name: 'MAVEN-3.6.3', type: 'maven'
    try {

        withEnv(["MVN_HOME=$mvnHome"])
        {
			
            withCredentials([usernamePassword(credentialsId: 'sonar-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
            {
                sh "'$MVN_HOME/bin/mvn' sonar:sonar -Dsonar.host.url=http://${env.SAST_Server_IP}:${env.Sonar_Port} /d:sonar.login=${USERNAME} /d:sonar.password=${PASSWORD} -X -DskipTests "
            }
             
            
        }

        notifier.sendMessage('','good','Stage: "SAST-Sonarqube": Sucess')

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