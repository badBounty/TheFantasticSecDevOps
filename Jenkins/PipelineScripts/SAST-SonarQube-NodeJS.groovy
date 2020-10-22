def runStage()
{
    try 
    {
        def projname = env.JOB_NAME
        
        sshagent(['ssh-key']) 
        {
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} /home/sonarscanner/bin/sonar-scanner -Dsonar.projectKey=${projname} -Dsonar.projectBaseDir=/home/${projname} -Dsonar.host.url=http://localhost:9000"
        }
    }
    catch(Exception e)
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "SAST-SonarQube": FAILURE'

        currentBuild.result = 'FAILURE'
        print('Stage "SAST-SonarQube": FAILURE')
        print(e.printStackTrace())
    }
}
return this