notifier = null

def Init(def notifierSetup)
{
    notifier = notifierSetup
}

def runStage(vulns)
{
    try
    {
        def vulnsTitles = ""
        def projname = env.JOB_NAME
        vulns.each{vuln ->
            def title = vuln[0]
            def description = vuln[1]
            def component = vuln[2]
            def line = vuln[3]
            def affected_code = vuln[4]
            def hash = vuln[5]
            def severity = vuln[6]
            def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
            def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
            def data = """{
                "Title": "$title",
                "Description": "$description",
                "Component": "$component",
                "Line": $line,
                "Affected_code": "$affected_code",
                "Commit": "$GIT_COMMIT",
                "Username": "$GIT_MAIL",
                "Pipeline_name": "$projname",
                "Language": "eng",
                "Hash": "$hash",
                "Severity_tool": "$severity"
            }"""
            try 
            {
                //POST The vul to orchestrator 
                res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.dashboardURL}"
                println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
            }
            catch (Exception e)
            {
                print("Internal error")
                print(data)
            }
            vulnsTitles = vulnsTitles + title + "\n"
            sh "sleep 1m"
        }
        notifier.sendMessage('','good',"Found Vulnerabilities:\n ${vulnsTitles}")
        
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-DependenciesChecks": FAILURE')

        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-Post": FAILURE')
        print(e.printStackTrace())
    }
}
