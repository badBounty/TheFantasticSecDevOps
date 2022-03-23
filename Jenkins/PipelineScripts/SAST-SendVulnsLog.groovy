def runStage(notifier, vulns)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-SendVulnsLog": INIT')
	    
        def projname = env.JOB_NAME
        def git_branch = env.branch
	    
	vulns.each
        { vuln ->
            def title = vuln[0]
            def line = vuln[3]
            def affected_code = vuln[4]
            String listAffectedCode = String.join(", ", affected_code);
            def affectedCodeParsed = listAffectedCode.tr(/"/,/'/);
            def severity = vuln[6]
            def origin = vuln[7]
		
	    def data = """{
                "Project": "${projname}",
		"Branch": "${git_branch}",
		"Vuln title": "${title}",
		"Severity_tool": "${severity.toLowerCase()}",
		"Affected_code": "${listAffectedCode}",
                "Line": "${line}",
                "Origin": "${origin}"
            }"""
            
            notifier.sendMessage('','#fab73c',"-----------------------------------------------------------------------------------------------")
	    notifier.sendMessage('','#fab73c',"${data}")
	}

        notifier.sendMessage('','good','Stage: "SAST-SendVulnsLog": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-SendVulnsLog": FAILURE')	
	currentBuild.result = 'FAILURE'
	print('Stage: "SAST-SendVulnsLog": FAILURE')
        print(e.printStackTrace())
    }
}

return this
