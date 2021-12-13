import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    try
    {
        notifier.sendMessage('','good','Stage: "SAST-SonarResults": INIT')

        def response = httpRequest "http://${env.SAST_Server_IP}:${env.Sonar_Port}/api/issues/search?p=1"
            print(response.status)
            def json = new JsonSlurperClassic().parseText(response.content)
            
            def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
            def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
            def projname = env.JOB_NAME
            json.issues.each{issue ->
                if (issue.type == 'VULNERABILITY' & issue.status == 'OPEN'){
                    print(issue)
                    def sev = issue.severity
                    def title = issue.rule
                    def message = issue.message.replaceAll('"', "'")
                    if (title.matches("[a-zA-Z0-9].*")){
                        def hash = issue.hash
                        def component = issue.component
                        if (issue.component.contains(":")){
                            component = issue.component.split(":").last()
                        }
                        
                        def line = issue.line
                        def affected_code = ""
                        try{
                            affected_code = sh(returnStdout: true, script: "sed '${line}!d' ${component}")
                        }
                        catch (Exception except)
                        {
                            affected_code = "none"
                        }
                        
                        def date = issue.updateDate.split('T')[0]
                        vulns.add([title, message, component, line, affected_code, hash, sev, "SonarQube"])
                    }
                }
            }

        notifier.sendMessage('','good','Stage: "SAST-SonarResults": SUCCESS')
    }
    catch(Exception e)
	{
		notifier.sendMessage('','danger','Stage: "SAST-SonarResults": FAILURE')
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-SonarResults": FAILURE')
		print(e.getMessage())
	}
}

return this
