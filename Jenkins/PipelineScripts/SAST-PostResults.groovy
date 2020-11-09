def runStage(notifier, vulns)
{
    notifier.sendMessage('','good','Stage: "SAST-PostResults": INIT')

    try
    {
        def projname = env.JOB_NAME
        def git_branch = env.branch
        def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
        notifier.sendMessage('','good',"Stage: SAST-PostResulst Found Vulnerabilities:")
        def startData = """{
            "Pipeline_name": "${projname}",
            "Branch": "${git_branch}",
            "Commit": "${GIT_COMMIT}"
        }"""
        try 
        {
            //POST The vul to orchestrator 
            res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: startData, url: "${env.Orchestrator_START_URL}"
            println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
            
        }
        catch (Exception e)
        {
            try
            {
                sh "sleep 1m"
                //POST The vul to orchestrator 
                res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: startData, url: "${env.Orchestrator_START_URL}"
                println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
            }
            catch (Exception ex)
            {
                print("Internal error")
                print(data)
            }
        }
        vulns.each
        { vuln ->
            def title = vuln[0]
            def description = vuln[1]
            def component = vuln[2]
            def line = vuln[3]
            def affected_code = vuln[4]
            def hash = vuln[5]
            def severity = vuln[6]
            def origin = vuln[7]
            
            def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
            
            def data = """{
                "Title": "${title}",
                "Description": "${description}",
                "Component": "${component}",
                "Line": ${line},
                "Affected_code": "${affected_code}",
                "Commit": "${GIT_COMMIT}",
                "Username": "${GIT_MAIL}",
                "Pipeline_name": "${projname}",
                "Branch": "${git_branch}",
                "Language": "eng",
                "Hash": "${hash}",
                "Severity_tool": "${severity}"
            }"""
            def vulnsTitle =  "Title: " + title + " Affected Resource: " + component + " Origin: " + origin
            try 
            {
                //POST The vul to orchestrator 
                res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.Orchestrator_POST_URL}"
                println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
                notifier.sendMessage('','#fab73c',"${vulnsTitle}")
            }
            catch (Exception e)
            {
                try
                {
                    sh "sleep 1m"
                    //POST The vul to orchestrator 
                    res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.Orchestrator_POST_URL}"
                    println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
                    notifier.sendMessage('','#fab73c',"${vulnsTitle}")
                }
                catch (Exception ex)
                {
                    print("Internal error")
                    print(data)
                }
            }

            
            sh "sleep 1m"
        }
        def endData = """{
            "Pipeline_name": "${projname}",
            "Branch": "${git_branch}",
            "Commit": "${GIT_COMMIT}"
        }"""
        try 
        {
            //POST The vul to orchestrator 
            res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: startData, url: "${env.Orchestrator_END_URL}"
            println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
            
        }
        catch (Exception e)
        {
            try
            {
                sh "sleep 1m"
                //POST The vul to orchestrator 
                res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: startData, url: "${env.Orchestrator_END_URL}"
                println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
            }
            catch (Exception ex)
            {
                print("Internal error")
                print(data)
            }
        }

        
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-PostResults": FAILURE')

        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-Post": FAILURE')
        print(e.printStackTrace())
    }
}

return this
