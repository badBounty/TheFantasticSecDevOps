def runStage(notifier, vulns)
{
    def pathPackageLockJson = env.PathPackageLockJson	
	
    try 
    {
        notifier.sendMessage('','good','Stage: "NodeJS-NPMAudit": INIT')

        def projname = env.JOB_NAME
	    
	def results = def resultNPMAudit = sh(script: "npm audit --json",returnStdout: true).trim()   
	print(results)
             
	/*
        def results = sh(script: "cat output.json", returnStdout: true).trim()
        def severity = sh(script: "cat severity.txt", returnStdout: true).trim()
        results = results.replace("\\", "")
        results = results.replace("\"", "\\\"")
        results = results.replace("\n", " ")
        if (severity == "Critical"){
            severity = "High"
        }
        vulns.add(["Outdated 3rd Party libraries", results, projname, 0, projname, "null", severity, "NPM-Audit"])
        */
	    
        notifier.sendMessage('','good','Stage: "NodeJS-NPMAudit": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "NodeJS-NPMAudit": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "NodeJS-NPMAudit": FAILURE')
        print(e.getMessage())
    }
}
return this
