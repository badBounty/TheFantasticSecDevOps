def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-SonarQube": INIT')

        def projname = env.JOB_NAME
        
        sshagent(['ssh-key-SAST-image']) 
        {
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/sonarscanner/bin/sonar-scanner -Dsonar.projectKey=${projname} -Dsonar.projectBaseDir=/home/${projname} -Dsonar.host.url=http://localhost:9000"
        }
        notifier.sendMessage('','good','Stage: "SAST-SonarQube": SUCCESS')
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