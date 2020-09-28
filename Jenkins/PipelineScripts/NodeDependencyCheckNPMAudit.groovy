def runStage(){
    try {
        sshagent(['ssh-key']) {
            def projname = env.JOB_NAME
            
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SASTIP}]:${env.port}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SASTIP}:/home"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} npm --prefix /home/${projname} audit --json > /home/npmaudit.json"
            sh """ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} /home/dependency-check/dependency-check/bin/dependency-check.sh --project '${projname}' --scan '/home/${projname}' --format CSV -o '/home/dependency-check.csv"""
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} python3 /home/parseDCandNPMAudit.py /home/dependency-check.csv /home/npmaudit.json /home/output.json"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/output.json ./output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}"
        }
        def results = sh(script: "cat output.json", returnStdout: true).trim()
        def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
        def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
        def data = """{
            "Title": "Outdated 3rd Party libraries"
            "Description": "$results",
            "Component": "$component",
            "Line": 0,
            "Affected_code": "",
            "Commit": "$GIT_COMMIT",
            "Username": "$GIT_MAIL",
            "Pipeline_name": "$projname",
            "Language": "eng",
            "Hash": "",
            "Severity_tool": "N/A",
        }"""
        print(data)
        res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.dashboardURL}"
        println(res.content)
        
        slackSend color: 'good', message: 'Depencency check & NPM audit: SUCCESS' 
        print('------Stage "NodeJSScan analysis": SUCCESS ------')
        
    }catch(Exception e) {
        currentBuild.result = 'FAILURE'
        slackSend color: 'danger', message: 'An error occurred in the "NodeJSScan analysis" stage' 
        print('------Stage "Depencency check & NPM audit": FAILURE ------')
    }
    
    
}