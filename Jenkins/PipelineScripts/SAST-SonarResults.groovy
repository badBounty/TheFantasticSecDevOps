import groovy.json.JsonSlurperClassic
vulns = [:]

def runStage()
{
    try
    {
        def pc = 1
        def ps = 100
        def total = 200
        while ((pc * ps) < total)
        {
            def response = httpRequest "http://${env.SASTIP}:${env.sonarport}/api/issues/search?p=${pc}&ps=${ps}"
            print(response.status)
            def json = new JsonSlurperClassic().parseText(response.content)
            
            def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
            def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
            def projname = env.JOB_NAME
            json.issues.each{issue ->
                if (issue.type == 'VULNERABILITY' & issue.status == 'OPEN'){
                    if (!vulns.containsKey(issue.rule)){
                        vulns[issue.rule] = []
                    }
                    def title = ""
                    def message = issue.message.replaceAll('"', "'")
                    sshagent(['ssh-key']) {
                        title = sh(returnStdout: true, script: "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} python3 /home/titleNormalization.py ${message}").trim()
                    }
                    def hash = issue.hash
                    def component = issue.component
                    if (issue.component.contains(":")){
                        component = issue.component.split(":")[1]
                    }
                    
                    def line = issue.line
                    def affected_code = sh(returnStdout: true, script: "sed '$line!d' $component")
                    def date = issue.updateDate.split('T')[0]
                    def sev = issue.severity
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
                        "Severity_tool": "$sev",
                    }"""

                    try
                    {
                        def res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.dashboardURL}"
                        println(res.content)
                    }
                    catch (Exception e)
                    {
                        //TODO use notifier module
                        slackSend color: 'danger', message: 'Stage: "SAST-SonarResults": FAILURE Send vuls to Orchestrator'

                        currentBuild.result = 'FAILURE'
                        print('Stage "SAST-SonarResults": FAILURE')
                        print(e.printStackTrace())
                        print(data)
                    }

                    vulns[issue.rule].add([message, component, line])

                    sleep(3)
                }
            }
            
            total = json.total
            pc += 1
        }
    }
    catch(Exception e)
	{
		//TODO use notifier module
		slackSend color: 'danger', message: 'Stage: "SAST-SonarResults": FAILURE'

		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-SonarResults": FAILURE')
		print(e.printStackTrace())
	}
}

def getVulnerabilities()
{
	return vulns
}

return this