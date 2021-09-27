def runStage(notifier, vulns)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-NodeJS-NPMAudit": INIT')

        def projname = env.JOB_NAME
        
        sshagent(['ssh-key-SAST-image']) 
        {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
            def resultFlawfinder = sh(script: "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} npm --prefix $1 audit --json > /home/npmaudit.json", returnStdout:true)
            writeFile(file: 'npmaudit.json', text: resultFlawfinder)
	        sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no ./npmaudit.json root@${env.SAST_Server_IP}:/home/npmaudit.json"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseNPMAuditResults.py /home/npmaudit.json /home/output.json /home/severity.txt"
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
        vulns.add(["Outdated 3rd Party libraries", results, projname, 0, projname, "null", severity, "NPM-Audit"])
        
        notifier.sendMessage('','good','Stage: "SAST-NodeJS-NPMAudit": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-NodeJS-NPMAudit": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-NodeJS-NPMAudit": FAILURE')
        print(e.getMessage())
    }
}
return this
