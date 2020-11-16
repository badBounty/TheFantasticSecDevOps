def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Deployment": INIT')

        sshagent(['ssh-key-SAST-server']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} screen -d -m ${env.SAST_Server_Repository_SAST_Path}/start.sh nobuild ${projname} ${env.Sonar_Port} ${env.SAST_Server_SSH_Port}"
            sh 'sleep 15m'
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} docker container ls -a"
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} ls -a -l"
        }

        notifier.sendMessage('','good','Stage: "SAST-Deployment": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Deployment": FAILURE')	
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Deployment": FAILURE')
        print(e.printStackTrace())
    }
}
return this