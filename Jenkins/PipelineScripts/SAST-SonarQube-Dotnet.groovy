def runStage(notifier)
{

    try
    {
        notifier.sendMessage('','good','Stage: "SAST-SonarQube": INIT')

        def projname = env.JOB_NAME
        withCredentials([usernamePassword(credentialsId: 'sonar-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
        {
            sh "/root/.dotnet/tools/dotnet-sonarscanner begin /k:${projname} /d:sonar.login=${USERNAME} /d:sonar.password=${PASSWORD} /d:sonar.host.url=http://${env.SAST_Server_IP}:${env.Sonar_Port}"
            sh 'find . -name *.sln -exec dotnet build {} ";"'
            sh "/root/.dotnet/tools/dotnet-sonarscanner end /d:sonar.login=${USERNAME} /d:sonar.password=${PASSWORD}"
        }

        notifier.sendMessage('','good','Stage: "SAST-SonarQube": SUCESS')
        
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