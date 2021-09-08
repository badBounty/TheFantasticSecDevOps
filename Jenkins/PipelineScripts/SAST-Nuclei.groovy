def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-Nuclei": INIT')

        sshagent(['ssh-key-SAST-image']) 
        {
            def projname = env.JOB_NAME
	    sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_Server_IP}:/home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cd /home"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mv /opt/sonarqube/nuclei /home"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/nuclei -ut"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} mkdir /$(pwd)/TheFantasticDevSecOps"
	    withCredentials([usernamePassword(credentialsId: 'git-code-token-clone', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
		{
		  sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} git clone https://${USERNAME}:${PASSWORD}@github.com/badBounty/TheFantasticSecDevOps.git /$(pwd)/TheFantasticDevSecOps" 
		}
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cp -rf /$(pwd)/TheFantasticDevSecOps/SAST/Nuclei-Custom-Templates/* /root/nuclei-templates/file"
	    sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /$(pwd)/TheFantasticDevSecOps/ -d -r"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} cp -a /home/Nuclei-Custom-Templates/. /root/nuclei-templates/file"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/nuclei -t /root/nuclei-templates/file -target /home/${projname} -o /home/nuclei-results.txt"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/nuclei-results.txt /home"
          
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
