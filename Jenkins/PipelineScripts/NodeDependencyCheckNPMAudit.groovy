def runStage(){
    try {
        def projname = env.JOB_NAME
        sshagent(['ssh-key']) {
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
        def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
        def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
        def data = """{
            "Title": "Outdated 3rd Party libraries",
            "Description": "$results",
            "Component": "$projname",
            "Line": 0,
            "Affected_code": "$projname",
            "Commit": "$GIT_COMMIT",
            "Username": "$GIT_MAIL",
            "Pipeline_name": "$projname",
            "Language": "eng",
            "Hash": "null",
            "Severity_tool": "MEDIUM"
        }"""
        try {
            res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.dashboardURL}"
            println(res.status)
        }
        catch (Exception e)
        {
            print('Internal server error')
            print(data)
        }
        slackSend color: 'good', message: 'Depencency check & NPM audit: SUCCESS' 
        print('------Stage "NodeJSScan analysis": SUCCESS ------')
    } catch(Exception e) {
        currentBuild.result = 'FAILURE'
        slackSend color: 'danger', message: 'An error occurred in the "NodeJSScan analysis" stage' 
        print('------Stage "Depencency check & NPM audit": FAILURE ------')
    }
}

return this