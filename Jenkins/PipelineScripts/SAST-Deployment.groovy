def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Deployment": INIT')

        sshagent(['ssh-key-SAST-server']) 
        {
            def projname = env.JOB_NAME
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]"
            //screen -d -m
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} ${env.SAST_Server_Repository_SAST_Path}/start.sh nobuild ${projname} ${env.Sonar_Port} ${env.SAST_Server_SSH_Port}"
            //sh 'sleep 15m'
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_Server_User}@${env.SAST_Server_IP} docker container ls -a"
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