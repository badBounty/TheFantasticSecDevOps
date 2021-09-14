def runStage(notifier, vulns)
{
    notifier.sendMessage('','good','Stage: "SAST-PostResults": INIT')

    try
    {
        def projname = env.JOB_NAME
        def git_branch = env.branch
        def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
        def resStatus = null
        
        notifier.sendMessage('','good',"Stage: SAST-PostResult Found Vulnerabilities:")
       
        //START DATA REGION
        
        def startData = """{
            "Pipeline_name": "${projname}",
            "Branch": "${git_branch}",
            "Commit": "${GIT_COMMIT}",
            "Status": "Start"
        }"""
        
        try 
        {
            //POST The vuln to orchestrator into START URL.
            res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: startData, url: "${env.Orchestrator_START_URL}"
            resStatus = res.status
        }
        catch (Exception ex)
        {
            print(ex.getMessage())
            print("Internal error in START")
            print(startData)
        }
        println("Stage: SAST-DependenciesChecks: Response status: "+resStatus+" en START URL")
                
        sh "sleep 1m"
        
        //POST DATA REGION        
                
        vulns.each
        { vuln ->
            def title = vuln[0]
            def description = vuln[1]
            def component = vuln[2]
            def line = vuln[3]
            def affected_code = vuln[4]
            //affected_code = affected_code.substring(0,Math.min(affected_code.length(),1000))
            def hash = vuln[5]
            def severity = vuln[6]
            def origin = vuln[7]
            
            def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
            
            def data = """{
                "Title": "${title}",
                "Description": "${description} - Origin: ${origin}",
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
            
            def vulnsTitle =  """Title: ${title} - Affected Resource: ${component} - Origin: ${origin}"""
            
            try 
            {
                //POST The vuln to orchestrator in POST URL.
                notifier.sendMessage('','#fab73c',"${vulnsTitle}")
                res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.Orchestrator_POST_URL}"
                resStatus = res.status  
            }
            catch (Exception exce)
            {
                print(ex.getMessage())
                print("Internal error in POST")
                print(data)
            }
            
            println("Stage: SAST-DependenciesChecks: Response status: "+resStatus+" en POST URL")
            
            sh "sleep 1m"
        }
                    
        //END DATA REGION             
               
        def endData = """{
            "Pipeline_name": "${projname}",
            "Branch": "${git_branch}",
            "Commit": "${GIT_COMMIT}",
            "Status": "End"
        }"""
                    
        try 
        {
            //POST The vul to orchestrator in END URL.
            res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: startData, url: "${env.Orchestrator_END_URL}"
            resStatus = res.status 
        }
        catch (Exception exc)
        {
            print(ex.getMessage())
            print("Internal error in END")
            print(endData)
        }
        println("Stage: SAST-DependenciesChecks: Response status: "+resStatus+" en END URL")
        
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "SAST-PostResults": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "SAST-Post": FAILURE')
        print(e.getMessage())
        print(e.printStackTrace())
    }
}

return this
