def runStage(notifier, vulns)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-DependencyChecks": INIT')

        def projname = env.JOB_NAME
        
        sshagent(['ssh-key-SAST-image']) 
        {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} /home/DependencyCheck.sh /home/${projname}/ ${projname}"
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
        vulns.add(["Outdated 3rd Party libraries", results, projname, 0, projname, "null", severity, "DependencyChecks"])
        
        notifier.sendMessage('','good','Stage: "SAST-DependencyChecks": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-DependencyChecks": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-DependencyChecks": FAILURE')
        print(e.getMessage())
    }
}
return this
