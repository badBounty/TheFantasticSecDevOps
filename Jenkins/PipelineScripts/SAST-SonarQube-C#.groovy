def runStage(){

    try {
        def projname = env.JOB_NAME
        sh "dotnet-sonarscanner begin /k:${projname} /d:sonar.login=${sonartoken} /d:sonar.host.url=${env.SASTIP}:${env.sonarport}"
        sh """find . -name \\"*.sln\\" -exec dotnet build \\\\\\;"""
        sh "dotnet-sonarscanner end"
         

        slackSend color: 'good', message: 'SonarQube analysis: SUCCESS' 
        print('------Stage "SonarQube analysis": SUCCESS ------')

    } catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "SonarQube analysis" stage' 
        print('------Stage "SonarQube analysis": FAILURE ------')

    } // try-catch-finally
}

return this