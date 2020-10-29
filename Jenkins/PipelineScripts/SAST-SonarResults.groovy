import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    try
    {
        notifier.sendMessage('','good','Stage: "SAST-SonarResults": INIT')

        def pc = 1
        def ps = 100
        def total = 200
        while ((pc * ps) < total)
        {
            def response = httpRequest "http://${env.SAST_Server_IP}:${env.Sonar_Port}/api/issues/search?p=${pc}&ps=${ps}"
            print(response.status)
            def json = new JsonSlurperClassic().parseText(response.content)
            
            def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
            def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
            def projname = env.JOB_NAME
            json.issues.each{issue ->
                if (issue.type == 'VULNERABILITY' & issue.status == 'OPEN'){
                    def sev = issue.severity
                    def title = issue.rule
                    def message = issue.message.replaceAll('"', "'")
                    sshagent(['ssh-key-SAST-image']) {
                        def normalizedInfo = sh(returnStdout: true, script: """ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/titleNormalization.py '${title}'""").trim().split("*")
                        title = normalizedInfo[0]
                        sev = normalizedInfo[1]
                    }
                    def hash = issue.hash
                    def component = issue.component
                    if (issue.component.contains(":")){
                        component = issue.component.split(":")[1]
                    }
                    
                    def line = issue.line
                    def affected_code = sh(returnStdout: true, script: "sed '$line!d' $component")
                    def date = issue.updateDate.split('T')[0]
                    

                    if (title.matches("[a-zA-Z0-9].*")){
                        vulns.add([title, message, component, line, affected_code, hash, sev, "SONARQUBE"])
                    }

                    
                }
            }

            notifier.sendMessage('','good','Stage: "SAST-SonarResults": SUCCESS')
            
            total = json.total
            pc += 1
        }
    }
    catch(Exception e)
	{
		notifier.sendMessage('','danger','Stage: "SAST-SonarResults": FAILURE')

		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-SonarResults": FAILURE')
		print(e.printStackTrace())
	}
}


return this