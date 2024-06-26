import groovy.json.JsonSlurperClassic

//Tool: Njsscan.
//Parsers: parseNodejsscan.py

def runStage(notifier, vulns)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-NodeJS": INIT')

        def projname = env.JOB_NAME
        sshagent(['ssh-key-SAST-image']) 
        {
            sh(script: "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} njsscan --json -o /home/njsscanPreparse.json /home/${projname}/",returnStatus: true) 
            //sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} njsscan --json -o /home/njsscanPreparse.json /home/${projname}/"  
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/parseNodejsscan.py /home/njsscanPreparse.json /home/output.json" 
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/output.json ./output.json"
            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} rm /home/output.json"
        }
        
        sh """sed -i -e 's/\\/home\\/${projname}\\///g' output.json"""
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
            /*
            sshagent(['ssh-key-SAST-image']) 
            {
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
            
            vulns.add([title, message, component, line, affected_code, hash, sev, "NodeJSScan"])
            
            /*
            if (title.matches("[a-zA-Z0-9].*"))
            {
                
            }
            */
           
        }
        sh 'rm output.json'

        notifier.sendMessage('','good','Stage: "SAST-NodeJS": SUCCESS')
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-NodeJS": FAILURE')

        currentBuild.result = 'FAILURE'
        print('Stage "SAST-NodeJS": FAILURE')
        print(e.getMessage())
    }
}

return this
