import groovy.json.JsonSlurperClassic
import java.text.SimpleDateFormat

vulns = [:]

def runStage(){
    try {
        sshagent(['ssh-key']) {
            def projname = env.JOB_NAME
            sh 'ssh-keygen -f "/var/jenkins_home/.ssh/known_hosts" -R [192.168.0.23]:44022'
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 rm -rf /home/${projname}/"
            sh 'scp -P 44022 -o StrictHostKeyChecking=no -r $(pwd) root@192.168.0.23:/home '
            sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package Puma.Security.Rules -v 2.3.0 \\\\\\;"""
            sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package SecurityCodeScan.VS2017 -v 2.3.0 \\\\\\;"""
            sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet clean {} \\\\\\;"""
            sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet build \\"/flp:logfile=/home/${projname}/build.log\\;verbosity=normal\\" \\"/flp1:logfile=/home/${projname}/errors.log\\;errorsonly\\" \\"/flp2:logfile=/home/${projname}/warnings.log\\;warningsonly\\" {} \\\\\\;"""
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.19 python3 /home/parseLog.py /home/${projname}/warnings.log /home/${projname}/issues.json"
            sh "scp -P 44022 -o StrictHostKeyChecking=no root@192.168.0.19:/home/${projname}/issues.json ."
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.19 rm /home/${projname}/issues.json"
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
    sec_vulns.each{issue ->
        def message = issue.message.replaceAll('"', "'")
        def component = issue.component
        def line = issue.affectedline
        def date = new Date()
        sdf = new SimpleDateFormat("yyyy-MM-dd")
        def date = sdf.format(date)
        def data = """{
            "Component": "$component",
            "Line": $line,
            "Message": "$message",
            "Date": "$date"
        }"""
        
        def res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: 'http://192.168.0.23:5000/api/issue'
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