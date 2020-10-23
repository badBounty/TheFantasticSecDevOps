def runStage()
{
    try 
    {
        def projname = env.JOB_NAME

        sshagent(['ssh-key'])
        {
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} python3 /home/vulRegexScanner.py \"/home/${projname}\" \"/home/regex.json\" \"/home/result.json\""
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} ls /home/"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/result.json ./result.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/result.json"
        }

        def vulns = [:]

        sh """sed -i -e 's/\\/home\\/${projname}\\///g' result.json"""

        def results = sh(script: "cat result.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)["results"]
        results = null

        def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
        def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()

        json.each{issue ->
            print(issue)
            def title = issue["title"]
            def message = issue["title"]
            def files = issue["affectedFiles"]
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
                        slackSend color: 'danger', message: 'Stage: "SAST-RegexScanner": FAILURE Send vuls to Orchestrator'
		
                        currentBuild.result = 'FAILURE'
                        print('Stage: "SAST-RegexScanner": FAILURE')
                        print(e.printStackTrace())
                    }

                    if (!vulns.containsKey(title))
                    {
                        vulns[title] = []
                    }

                    vulns[title].add([message, component, line])
                    sh "sleep 1m"
                }
            }
            sh 'rm result.json'
        }
    }
    catch(Exception e)
    {
        //TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "SAST-RegexScanner": FAILURE'
		
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-RegexScanner": FAILURE')
		print(e.printStackTrace())
    }
}
return this