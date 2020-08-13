import groovy.json.JsonSlurperClassic
import java.text.SimpleDateFormat

vulns = [:]

def runStage(){
    try {
        sshagent(['ssh-key']) {
            def projname = env.JOB_NAME
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SASTIP}]:${env.port}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}/"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no -r $(pwd) root@${env.SASTIP}:/home "
            sh """ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package Puma.Security.Rules -v 2.3.0 \\\\\\;"""
            sh """ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package SecurityCodeScan.VS2017 -v 2.3.0 \\\\\\;"""
            sh """ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet clean {} \\\\\\;"""
            sh """ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet build \\"/flp:logfile=/home/${projname}/build.log\\;verbosity=normal\\" \\"/flp1:logfile=/home/${projname}/errors.log\\;errorsonly\\" \\"/flp2:logfile=/home/${projname}/warnings.log\\;warningsonly\\" {} \\\\\\;"""
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} python3 /home/parseLog.py /home/${projname}/warnings.log /home/${projname}/issues.json"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/${projname}/issues.json ."
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/${projname}/issues.json"
        }

        slackSend color: 'good', message: 'C# analysis: SUCCESS' 
        print('------Stage "C# analysis": SUCCESS ------')
    }catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "C# analysis" stage' 
        print('------Stage "C# analysis": FAILURE ------')

    }
 }


@NonCPS
def parseVulns() {
    def results = sh(script: "cat issues.json", returnStdout: true).trim()
    def sec_vulns = parse(results)
    def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
    def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
    def projname = env.JOB_NAME
    sec_vulns.each{issue ->
        def title = ""
        def message = issue.message.replaceAll('"', "'")
        sshagent(['ssh-key']) {
            title = sh(returnStdout: true, script: "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} python3 /home/parseLog.py ${message}").trim()
        }
        def component = issue.component
        def line = issue.affectedline
        def affected_code = sh(returnStdout: true, script: "sed '$line!d' $component")
        def hash = sh(returnStdout: true, script: "sha256sum reboothitron.sh $component | awk 'NR==1{print $1}'")
        def date = sdf.format(new Date())
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
        vulns[issue.rule].add([message, component, line])
        sleep(3)
    }
    sh 'rm issues.json'

}

@NonCPS
def parse(def results){
    def json = new JsonSlurperClassic().parseText(results)

    sec_vulns = json
    json = null
    return sec_vulns
}

def getResults(){
    return vulns
}

return this