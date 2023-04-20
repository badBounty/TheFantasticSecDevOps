import groovy.json.JsonSlurperClassic
def runStage() {
    try {
        sshagent(['ssh-key-SAST-image']) {
            sh(script: "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} njsscan --json -o /home/njsscanPreparse.json /home/${env.REPO_TO_SCAN_NAME}/",returnStatus: true) 
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} python3 /home/parseNodejsscan.py /home/njsscanPreparse.json /home/output.json" 
            sh "scp -P ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP}:/home/output.json ./output.json"
            sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} rm /home/output.json"
        }
        sh """sed -i -e 's/\\/home\\/${env.REPO_TO_SCAN_NAME}\\///g' output.json"""
        def results = sh(script: "cat output.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)
        results = null

        json.each{issue ->
            print(issue)
            def title = issue["title"]
            def message = issue["title"]
            def component = issue["file"]
            def line = issue['lineNumber']
            def affected_code = issue['line']
            affected_code = affected_code.replace("\\", "")
            affected_code = affected_code.replace("\"", "\\\"")
            affected_code = affected_code.replace("\n", " ")
            def hash = sh(returnStdout: true, script: "sha256sum \$(pwd)/${component} | awk 'NR==1{print \$1}'")
            def sev = "Low" 
            hash = hash.replace("\n", " ")
            vulns.add([title, message, component, line, affected_code, hash, sev, "NodeJSScan"])
        }
        sh 'rm output.json'
        print('Stage "SAST-NodeJS": SUCCESS')
    }
    catch(Exception e) {
        currentBuild.result = 'FAILURE'
        print('Stage "SAST-NodeJS": FAILURE')
        print(e.getMessage())
    }
}

return this
