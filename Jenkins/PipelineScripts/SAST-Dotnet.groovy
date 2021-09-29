import groovy.json.JsonSlurperClassic
import java.text.SimpleDateFormat

//Tools: PumaScan, SecurityCodeScan
//Parser: parseLog.py

def runStage(notifier, vulns)
{
    try {

        notifier.sendMessage('','good','Stage: "SAST-Dotnet": INIT')
                
        sshagent(['ssh-key-SAST-image'])
        {
            def projname = env.JOB_NAME
            sh """scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no -r \$(pwd) root@${env.SAST_Server_IP}:/home """
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package Puma.Security.Rules -v 2.3.0 \\\\\\;"""
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package SecurityCodeScan.VS2017 -v 2.3.0 \\\\\\;"""
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet clean {} \\\\\\;"""
            sh """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet build \\"/flp:logfile=/home/${projname}/build.log\\;verbosity=normal\\" \\"/flp1:logfile=/home/${projname}/errors.log\\;errorsonly\\" \\"/flp2:logfile=/home/${projname}/warnings.log\\;warningsonly\\" {} \\\\\\;"""
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseLog.py /home/${projname}/warnings.log /home/${projname}/issues.json"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/${projname}/issues.json ."
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/${projname}/issues.json"
        }
        
        def results = sh(script: "cat issues.json", returnStdout: true).trim()
        def sec_vulns =  new JsonSlurperClassic().parseText(results)
        results = null
        sec_vulns.each{issue ->
            
            def title = issue["title"]
            def message = issue["message"]
            def component = issue["file"]
            def line = issue['lineNumber']
            def sev = "Low"
            def affected_code = ""
            def hash = ""
            /*
            sshagent(['ssh-key-SAST-image']) {
                try{
                    def normalizedInfo = sh(returnStdout: true, script: """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/titleNormalization.py '${title}'""").trim().split("""\\*""")
                    title = normalizedInfo[0]
                    sev = normalizedInfo[1]
                }catch (Exception ex)
                {
                    title = ""
                    sev = ""
                }
            }
            */
            if (title.matches("[a-zA-Z0-9].*")){
                try{
                    affected_code = sh(returnStdout: true, script: "sed '$line!d' $component")
                    hash = sh(returnStdout: true, script: """sha256sum reboothitron.sh ${component} | awk 'NR==1{print \$1}'""")
                }catch (Exception ex)
                {
                    affected_code = ""
                    hash = ""
                }
                
                vulns.add([title, message, component, line, affected_code, hash, sev, "Puma"])
            }
        }
        
        sh 'rm issues.json'
        notifier.sendMessage('','good','Stage: "SAST-Dotnet": SUCCESS')
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-Dotnet": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage "SAST-Dotnet": FAILURE')
        print(e.printStackTrace())
    }
}

return this
