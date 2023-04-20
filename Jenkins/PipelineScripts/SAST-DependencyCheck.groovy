def runStage() {
    try {
        sshagent(['ssh-key-SAST-image']) {
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_SERVER_IP}]:${env.SAST_SERVER_SSH_PORT}"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} /home/DependencyCheck.sh /home/${env.REPO_TO_SCAN_NAME}/ ${env.REPO_TO_SCAN_NAME}"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} python3 /home/parseDependencyCheckResults.py /home/dependency-check.csv /home/outputDepCheck.json"
            sh "scp -P ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP}:/home/outputDepCheck.json ./outputDepCheck.json"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} rm /home/outputDepCheck.json"
        }
        def results = sh(script: "cat outputDepCheck.json", returnStdout: true).trim()
        results = results.replace("\\", "")
        results = results.replace("\"", "\\\"")
        results = results.replace("\n", " ")
        vulns.add(["Outdated 3rd Party libraries", results, ${env.REPO_TO_SCAN_NAME}, 0, ${env.REPO_TO_SCAN_NAME}, "null", "High", "DependencyChecks"])
        print('Stage: "SAST-DependencyChecks": SUCCESS')
    } 
    catch(Exception e) {
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-DependencyChecks": FAILURE')
        print(e.getMessage())
    }
}
return this
