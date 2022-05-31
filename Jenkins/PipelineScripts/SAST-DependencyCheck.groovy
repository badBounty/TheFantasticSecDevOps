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
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseDependencyCheckResults.py /home/dependency-check.csv /home/outputDepCheck.json"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/outputDepCheck.json ./outputDepCheck.json"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/outputDepCheck.json"
        }

        def results = sh(script: "cat outputDepCheck.json", returnStdout: true).trim()
        results = results.replace("\\", "")
        results = results.replace("\"", "\\\"")
        results = results.replace("\n", " ")
        vulns.add(["Outdated 3rd Party libraries", results, projname, 0, projname, "null", "High", "DependencyChecks"])
        
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
