notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}

def runStage()
{

    try
    {
        def projname = env.JOB_NAME
        sh "/root/.dotnet/tools/dotnet-sonarscanner begin /k:${projname} /d:sonar.login=${env.sonartoken} /d:sonar.host.url=http://${env.SASTIP}:${env.sonarport}"
        sh 'find . -name *.sln -exec dotnet build {} ";"'
        sh "/root/.dotnet/tools/dotnet-sonarscanner end /d:sonar.login=${env.sonartoken}"
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