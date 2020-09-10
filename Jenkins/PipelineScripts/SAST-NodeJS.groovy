import groovy.json.JsonSlurperClassic

def runStage(){
    try {
        sshagent(['ssh-key']) {
            def projname = env.JOB_NAME
            
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SASTIP}]:${env.port}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no -v -r \$(pwd) root@${env.SASTIP}:/home"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} nodejsscan -d /home/${projname} -o /home/output.json"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/output.json ./output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}/*"
        }
        
        slackSend color: 'good', message: 'NodeJSScan analysis: SUCCESS' 
        print('------Stage "NodeJSScan analysis": SUCCESS ------')

    }catch(Exception e) {
        
        currentBuild.result = 'FAILURE'
        slackSend color: 'danger', message: 'An error occurred in the "NodeJSScan analysis" stage' 
        print('------Stage "NodeJSScan analysis": FAILURE ------')
    }
    
    
}

@NonCPS
def parse(def results){
    def json = new JsonSlurperClassic().parseText(results)
    
    //print(json)
    sec_vulns = json.sec_issues["Application Related"]
    json = null
    return sec_vulns
}

@NonCPS
def getResults(){

    def vulns = [:]
    
    def results = sh(script: "cat output.json", returnStdout: true).trim()
    def sec_vulns = parse(results)
    results = null
    def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
    def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
    def projname = env.JOB_NAME
    sec_vulns.each{issue ->
        def title = issue["title"]
        def message = issue["description"]
        def component = issue["path"]
        def line = issue.affectedline
        def affected_code = ""
        def hash = ""
        sshagent(['ssh-key']) {
            title = sh(returnStdout: true, script: "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} python3 /home/titleNormalization.py ${title}").trim()
            affected_code = sh(returnStdout: true, script: "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} sed '${line}!d' ${component}")
            hash = sh(returnStdout: true, script: "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} sha256sum reboothitron.sh ${component} | awk 'NR==1{print \$1}'")    
        }
        def data = """{
            "Title": "$title"
            "Description": "$message",
            "Component": "$component",
            "Line": $line,
            "Affected_code": "$affected_code",
            "Commit": "$GIT_COMMIT",
            "Username": "$GIT_MAIL",
            "Pipeline_name": "$projname",
            "Language": "eng",
            "Hash": "$hash",
            "Severity_tool": "N/A",
        }"""
        def res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.dashboardURL}"
        println(res.content)
        if (!vulns.containsKey(title)){
            vulns[title] = []
        }
        vulns[title].add([message, component, line])

    }
    
    sh 'rm output.json'
}

return this