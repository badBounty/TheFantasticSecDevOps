import groovy.json.JsonSlurperClassic

def runStage(notifier)
{
    def projname = env.JOB_NAME
    try 
    {
        notifier.sendMessage('','good','Stage: "SAST-SCA-Java": INIT')

        def results = sh(script: "mvn dependency:tree -DoutputType=dot", returnStdout: true)
        print('Maven Libraries: \n')
        print('Format --> Group:Artifact:Type:Version:Scope \n')
        print(results)
		    
        notifier.sendMessage('','good','Stage: "SAST-SCA-Java": SUCCESS')
    }
    catch(Exception e) 
    {
        notifier.sendMessage('','danger','Stage: "SAST-SCA-Java": FAILURE')	
        currentBuild.result = 'FAILURE'
	      print('Stage: "SAST-SCA-Java": FAILURE')
        print(e.getMessage())
    }
}

return this
