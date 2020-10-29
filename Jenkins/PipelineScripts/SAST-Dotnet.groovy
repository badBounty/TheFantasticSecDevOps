import groovy.json.JsonSlurperClassic
import java.text.SimpleDateFormat

def runStage()
{
    try {
        sshagent(['ssh-key-SAST-image'])
        {
            def projname = env.JOB_NAME
            sh "ssh-keygen -f '/var/jenkins_home/.ssh/known_hosts' -R [${env.SAST_Server_IP}]:${env.SAST_Server_SSH_Port}"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm -rf /home/${projname}/"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -r $(pwd) root@${env.SAST_Server_IP}:/home "
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package Puma.Security.Rules -v 2.3.0 \\\\\\;"""
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package SecurityCodeScan.VS2017 -v 2.3.0 \\\\\\;"""
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet clean {} \\\\\\;"""
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet build \\"/flp:logfile=/home/${projname}/build.log\\;verbosity=normal\\" \\"/flp1:logfile=/home/${projname}/errors.log\\;errorsonly\\" \\"/flp2:logfile=/home/${projname}/warnings.log\\;warningsonly\\" {} \\\\\\;"""
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseLog.py /home/${projname}/warnings.log /home/${projname}/issues.json"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/${projname}/issues.json ."
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/${projname}/issues.json"
        }

        parseVulns()

    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-Dotnet": FAILURE')

        currentBuild.result = 'FAILURE'
        print('Stage "SAST-Dotnet": FAILURE')
        print(e.printStackTrace())
    }
 }


@NonCPS
def parseVulns()
{
    def results = sh(script: "cat issues.json", returnStdout: true).trim()
    def sec_vulns =  new JsonSlurperClassic().parseText(results)
    results = null
    def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
    def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
    def projname = env.JOB_NAME
    println(sec_vulns)
    sec_vulns.each{k, issue ->
        def title = ""
        def rule = k
        def message = issue.message.replaceAll('"', "'")
        sshagent(['ssh-key-SAST-image']) {
            title = sh(returnStdout: true, script: "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/titleNormalization.py ${rule}").trim()
        }
        if (title.matches("[a-zA-Z0-9].*")){
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
            def res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.Orchestrator_POST_URL}"
            println(res.content)
            vulns[issue.rule].add([message, component, line])
            sleep(3)
        }
        
    }

    sh 'rm issues.json'
}

return this