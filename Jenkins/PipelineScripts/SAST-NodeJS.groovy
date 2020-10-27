import groovy.json.JsonSlurperClassic

notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}

def runStage(vulns)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-NodeJS": INIT')

        def projname = env.JOB_NAME
        sshagent(['ssh-key']) 
        {

            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} /home/NodeScan.sh /home/${projname}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} ls /home/"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/output.json ./output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/output.json"
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
            hash = hash.replace("\n", " ")
            sshagent(['ssh-key']) 
            {
                title = sh(returnStdout: true, script: """ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} python3 /home/titleNormalization.py '${title}'""").trim()
            }
            
            
            if (title.matches("[a-zA-Z0-9].*"))
            {
                vulns.add([title, message, component, line, affected_code, hash, "LOW"])
            }
           
        }
        sh 'rm output.json'

        notifier.sendMessage('','good','Stage: "SAST-NodeJS": SUCCESS')
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-NodeJS": FAILURE')

        currentBuild.result = 'FAILURE'
        print('Stage "SAST-NodeJS": FAILURE')
        print(e.printStackTrace())
    }
}
return this
