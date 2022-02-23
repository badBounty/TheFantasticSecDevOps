import groovy.json.JsonSlurperClassic

def runStage(notifier, vulns)
{
    notifier.sendMessage('','good','Stage: "SAST-PostResults": INIT')

    try
    {
        def projname = env.JOB_NAME
        def git_branch = env.branch
        def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
        def resStatus = null
        
        def severityNormalized = new JsonSlurperClassic().parseText('''{
            "major": "high",
            "very high": "high",
            "critical": "high",
            "normal": "medium",
            "regular": "medium",
            "error": "low",
            "info": "low",
            "minor": "low",
            "informational": "low",
            "code_smell": "low"
        }''')
        
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
        println("Stage: SAST-PostResults: Response status: "+resStatus+" en START URL")
                
        sh "sleep ${env.sleepTimePostResults}"
        
        //POST DATA REGION        
                
        vulns.each
        { vuln ->
            def title = vuln[0]
            def description = vuln[1]
            def component = vuln[2]
            def line = vuln[3]
            def affected_code = vuln[4]
            String listAffectedCode = String.join(", ", affected_code);
            listAffectedCode = listAffectedCode.bytes.encodeBase64().toString()
            def hash = vuln[5]
            def severity = vuln[6]
            def origin = vuln[7]
            
            def GIT_MAIL = sh(returnStdout: true, script: 'git show -s --format=%ae').trim()
            
            if(severity.toLowerCase() in severityNormalized){
                severity = severityNormalized[severity.toLowerCase()]
            }
            
            def data = """{
                "Title": "${title}",
                "Description": "${description} - Origin: ${origin}",
                "Component": "${component}",
                "Line": "${line}",
                "Affected_code": "${listAffectedCode}",
                "Commit": "${GIT_COMMIT}",
                "Username": "${GIT_MAIL}",
                "Pipeline_name": "${projname}",
                "Branch": "${git_branch}",
                "Language": "eng",
                "Hash": "${hash}",
                "Severity_tool": "${severity}"
            }"""
            
            def vulnsTitle =  """Title: ${title} - Affected Resource: ${component} - Origin: ${origin}"""
            
            print(data)
            
            try 
            {
                print(severity)
                //POST The vuln to orchestrator in POST URL.
                if(severity != null || severity != "Null"){
                    notifier.sendMessage('','#fab73c',"${vulnsTitle}")
                    notifier.sendMessage('','#fab73c',"${data}")
                    res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.Orchestrator_POST_URL}"
                    resStatus = res.status  
                } 
            }
            catch (Exception exce)
            {
                print(ex.getMessage())
                print("Internal error in POST")
                print(data)
            }
            
            println("Stage: SAST-PostResults: Response status: "+resStatus+" en POST URL")
            
            sh "sleep ${env.sleepTimePostResults}"
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
        println("Stage: SAST-PostResults: Response status: "+resStatus+" en END URL")
        
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
