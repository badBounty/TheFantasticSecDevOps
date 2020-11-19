def runStage(notifier)
{

    notifier.sendMessage('','good','Stage: "SAST-Sonarqube": INIT')

    try {

        withMaven
		{
			
	        
			
            withCredentials([usernamePassword(credentialsId: 'sonar-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
            {
                sh "mvn sonar:sonar -Dsonar.host.url=http://${env.SAST_Server_IP}:${env.Sonar_Port} /d:sonar.login=${USERNAME} /d:sonar.password=${PASSWORD} -X -DskipTests "
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