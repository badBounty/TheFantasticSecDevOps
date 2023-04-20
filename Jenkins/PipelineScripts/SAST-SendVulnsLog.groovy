def runStage() {
    try {
	def vulnsParsed = []    
	vulns.each {
		vuln ->
            def title = vuln[0]
            def description = vuln[1]
	    	def component = vuln[2]
            def line = vuln[3]
            def affected_code = vuln[4]
            String listAffectedCode = String.join(", ", affected_code);
            def affectedCodeParsed = listAffectedCode.tr(/"/,/'/);
            def severity = vuln[6]
            def origin = vuln[7]		
		    def data = """{
			"Project": "${env.REPO_TO_SCAN_NAME}",
			"Branch": "${env.REPO_TO_SCAN_BRANCH}",
			"Vuln title": "${title}",
			"Description": "${description}",
			"Severity_tool": "${severity.toLowerCase()}",
			"Affected_code": "${listAffectedCode}",
			"Component": "${component}",
			"Line": "${line}",
			"Origin": "${origin}"
		    }"""
            	    print(data)
		
	    if(!affected_code.isEmpty()){
		vulnsParsed.add(data)
	    }
	}
	writeFile(file: 'vulnsParsed.json', text: vulnsParsed.join(", "))

        print('"SAST-SendVulnsLog": SUCCESS')
    }
    catch(Exception e) {	
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-SendVulnsLog": FAILURE')
        print(e.printStackTrace())
    }
}
return this
