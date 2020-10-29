def runStage(notifier)
{

    try
    {
        def projname = env.JOB_NAME
        sh "/root/.dotnet/tools/dotnet-sonarscanner begin /k:${projname} /d:sonar.login=${env.Sonar_Token} /d:sonar.host.url=http://${env.SAST_Server_IP}:${env.Sonar_Port}"
        sh 'find . -name *.sln -exec dotnet build {} ";"'
        sh "/root/.dotnet/tools/dotnet-sonarscanner end /d:sonar.login=${env.Sonar_Token}"
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