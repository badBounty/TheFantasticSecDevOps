import groovy.json.JsonSlurperClassic


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

    def results = sh(script: "cat issues.json", returnStdout: true).trim()
    def vulns = parse(results)
    
    print(vulns)
    sh 'rm issues.json'
}

return this