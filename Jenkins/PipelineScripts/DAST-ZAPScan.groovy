import groovy.json.JsonSlurperClassic

def runStage(notifier)
{
    try 
    {
        notifier.sendMessage('','good','Stage: "DAST": INIT')

        def projname = env.JOB_NAME
        sshagent(['ssh-key-DAST-image']) 
        {

            sh "ssh -p ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP} python3 /home/zap/ZAPScanner.py ${env.DAST_Server_SSH_Port}"
            sh "scp -P ${env.SAST_Server_SSH_Port} -o StrictHostKeyChecking=no root@${env.SAST_Server_IP}:/home/zap/output.json ./output.json"
        }
        
        def results = sh(script: "cat output.json", returnStdout: true).trim()
        def json = new JsonSlurperClassic().parseText(results)
        results = null

        json.each{issue ->
            print(issue)
            def title = issue["name"]
            def description = issue["description"]
            def domain = env.target
            def resource = issue['url']
            def severity = issue['risk']
            
            
            def data = """{​​​​​​​​
                "Title": "${title}",
                "Description": "${description}",
                "Domain": "${domain}",
                "Resource": "${resource}",
                "Severity": "${severity}"
            }​​​​​​​​"""
            try 
            {
                //POST The vul to orchestrator 
                res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.Orchestrator_POST_URL}"
                println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
                notifier.sendMessage('','#fab73c',"${title}")
            }
            catch (Exception exce)
            {
                try
                {
                    sh "sleep 1m"
                    //POST The vul to orchestrator 
                    res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "${env.Orchestrator_POST_URL}"
                    println("Stage: SAST-DependenciesChecks: Response status: "+res.status)
                    notifier.sendMessage('','#fab73c',"${title}")
                }
                catch (Exception exc)
                {
                    print("Internal error")
                    print(data)
                }
            }
           
        }
        sh 'rm output.json'

        notifier.sendMessage('','good','Stage: "DAST": SUCCESS')
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "DAST": FAILURE')

        currentBuild.result = 'FAILURE'
        print('Stage "DAST": FAILURE')
        print(e.printStackTrace())
    }
}
return this