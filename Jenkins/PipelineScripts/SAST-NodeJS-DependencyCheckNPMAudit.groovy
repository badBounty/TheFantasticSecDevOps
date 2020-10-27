notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}

def runStage(vulns)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-DependenciesChecks": INIT')

        def projname = env.JOB_NAME
        
        sshagent(['ssh-key']) 
        {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SASTIP}]:${env.port}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} chmod 777 /home/dependencies.sh"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} chmod 777 /home/NodeScan.sh"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SASTIP}:/home"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} ls /home/"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} /home/dependencies.sh /home/${projname}/ ${projname}"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/output.json ./output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/output.json"
        }

        def results = sh(script: "cat output.json", returnStdout: true).trim()
        results = results.replace("\\", "")
        results = results.replace("\"", "\\\"")
        results = results.replace("\n", " ")
        vulns.add(["Outdated 3rd Party libraries", $results, $projname, 0, $projname, "null", "MEDIUM"])
        
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