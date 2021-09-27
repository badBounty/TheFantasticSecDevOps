
import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    try
    {
        notifier.sendMessage('','good','Stage: "SAST-RegexScanner": INIT')
        
        def projname = env.JOB_NAME
        
        sshagent(['ssh-key-SAST-image'])
        {
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/vulRegexScanner.py /home/${projname} /home/regex.json /home/result.json"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} ls /home/"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/result.json ./result.json"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/result.json"
        }
        sh """sed -i -e 's/\\/home\\/${projname}\\///g' result.json"""
        
        def results = sh(script: "cat result.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)["results"]
        results = null
        
        json.each{issue ->
            def title = issue["title"]
            def message = issue["title"]
            def files = issue["affectedFiles"]
            def sev = issue["risk"]
            files.each{file -> 
                def component = file["file"]
                def line = file['lineNumber']
                def affected_code = file['line']
                
                affected_code = affected_code.replace("\\", "")
                affected_code = affected_code.replace("\"", "\\\"")
                affected_code = affected_code.replace("\n", " ")
                def hash = sh(returnStdout: true, script: "sha256sum \$(pwd)/${component} | awk 'NR==1{print \$1}'")    
                hash = hash.replace("\n", " ")
                if (title.matches("[a-zA-Z0-9].*")){
                    vulns.add([title, message, component, line, affected_code, hash, sev, "RegexScanner"])
                }
            }
        }
        
        notifier.sendMessage('','good','Stage: "SAST-RegexScanner": SUCCESS')
        sh 'rm result.json'
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-RegexScanner": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-RegexScanner": FAILURE')
        print(e.printStackTrace())
    }
}
return this
