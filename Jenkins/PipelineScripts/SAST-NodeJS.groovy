import groovy.json.JsonSlurperClassic

def runStage()
{
    try 
    {
        def projname = env.JOB_NAME
        sshagent(['ssh-key']) 
        {

            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} /home/NodeScan.sh /home/${projname}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} ls /home/"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/output.json ./output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/output.json"
        }
        
        def vulns = [:]
        sh """sed -i -e 's/\\/home\\/${projname}\\///g' output.json"""
        def results = sh(script: "cat output.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)
        results = null
        def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
        def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
        json.each
        {issue ->
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
            
            
            if (title.matches("[a-zA-Z0-9].*")){
                def data = """{
                    "Title": "$title",
                    "Description": "$message",
                    "Component": "$component",
                    "Line": $line,
                    "Affected_code": "$affected_code",
                    "Commit": "$GIT_COMMIT",
                    "Username": "$GIT_MAIL",
                    "Pipeline_name": "$projname",
                    "Language": "eng",
                    "Hash": "$hash",
                    "Severity_tool": "LOW"
                }"""
                
                try 
                {
                    res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.dashboardURL}"
                    println(res.status)
                }
                catch (Exception e)
                {
                    //TODO use notifier module
                    slackSend color: 'danger', message: 'Stage: "SAST-NodeJS": FAILURE'

                    currentBuild.result = 'FAILURE'
                    print('Stage "SAST-NodeJS": FAILURE')
                    print(e.printStackTrace())
                    print(data)
                }
                if (!vulns.containsKey(title))
                {
                    vulns[title] = []
                }
                vulns[title].add([message, component, line])
                sh "sleep 1m"
            }
           
        }
        sh 'rm output.json'
    }
    catch(Exception e)
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "SAST-NodeJS": FAILURE'

        currentBuild.result = 'FAILURE'
        print('Stage "SAST-NodeJS": FAILURE')
        print(e.printStackTrace())
    }
}
return this
