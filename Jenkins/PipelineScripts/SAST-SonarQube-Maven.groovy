def runStage(notifier)
{

    notifier.sendMessage('','good','Stage: "SAST-Sonarqube": INIT')

    try {

        withCredentials([usernamePassword(credentialsId: 'sonar-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
        {
            sshagent(['ssh-key-SAST-image']) 
            {
                sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mvn sonar:sonar -Dsonar.host.url=http://localhost:${env.Sonar_Port} -Dsonar.projectKey=${projname} -Dsonar.projectBaseDir=/home/${projname} -Dsonar.login=${USERNAME} -Dsonar.password=${PASSWORD} -X -DskipTests "
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
