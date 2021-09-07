def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Nuclei": INIT')

        sshagent(['ssh-key-SAST-image']) 
        {
            def projname = env.JOB_NAME
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} export PATH=$PATH:/usr/local/go/bin"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} git clone ${env.Nuclei_Repo_URL}"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd nuclei/v2/cmd/nuclei"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} go build"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mv nuclei /usr/local/bin"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} nuclei -t /home/nuclei-templates/ -target /home/${projname} -o /home/nuclei-results.txt"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/nuclei-results.txt ./nuclei-results.txt"
          
          //Migrar regexScanner.
        }

        notifier.sendMessage('','good','Stage: "SAST-Nuclei": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-Nuclei": FAILURE')	
        
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Nuclei": FAILURE')
        print(e.printStackTrace())
    }
}
return this
