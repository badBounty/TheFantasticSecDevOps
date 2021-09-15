def runStage(notifier, vulns)
{
    try 
    {
    
        notifier.sendMessage('','good','Stage: "SAST-DependenciesChecks": INIT')
                
        def projname = env.JOB_NAME
                        
        sshagent(['ssh-key-SAST-image']) 
        {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm -rf /home/${projname}"
            //Primero nuclei para realizar un scp del project una sola vez
            //sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SAST_Server_IP}:/home"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} ls /home/"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/dependency-check/dependency-check/bin/dependency-check.sh --project ${projname} --scan /home/${projname}/ --format CSV -o '/home/dependency-check.csv'"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseDCandNPMAudit.py /home/dependency-check.csv null /home/output.json /home/severity.txt"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/output.json ./output.json"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/severity.txt ./severity.txt"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/output.json"
        }
        
        def results = sh(script: "cat output.json", returnStdout: true).trim()
        
        def severity = sh(script: "cat severity.txt", returnStdout: true).trim()
        results = results.replace("\\", "")
        results = results.replace("\"", "\\\"")
        results = results.replace("\n", " ")
        if (severity == "Critical"){
            severity = "High"
        }
        vulns.add(["Outdated 3rd Party libraries", results, projname, 0, projname, "null", severity, "DependenciesCheck"])
        
        notifier.sendMessage('','good','Stage: "SAST-DependenciesChecks": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-DependenciesChecks": FAILURE')
        
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-DependenciesChecks": FAILURE')
        print(e.printStackTrace())
    }
}
return this
