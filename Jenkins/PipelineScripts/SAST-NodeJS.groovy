import groovy.json.JsonSlurperClassic

def runStage(){
    try {
        sshagent(['ssh-key']) {
            def projname = env.JOB_NAME
            sh 'ls'
            
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} ls /home/"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no -r $(pwd) root@${env.SASTIP}:/home"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} nodejsscan -d /home/${projname} -o /home/output.json"
            sh "scp -P ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP}:/home/output.json ./output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm /home/output.json"
            sh "ssh -p ${env.port} -o StrictHostKeyChecking=no root@${env.SASTIP} rm -rf /home/${projname}/*"
        }

        slackSend color: 'good', message: 'NodeJSScan analysis: SUCCESS' 
        print('------Stage "NodeJSScan analysis": SUCCESS ------')
    }catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "NodeJSScan analysis" stage' 
        print('------Stage "NodeJSScan analysis": FAILURE ------')

    }
    
    
}

@NonCPS
def parse(def results){
    def json = new JsonSlurperClassic().parseText(results)
    
    //print(json)
    sec_vulns = json.sec_issues["Application Related"]
    json = null
    return sec_vulns
}

@NonCPS
def getResults(){

    def vulns = [:]

    def results = sh(script: "cat output.json", returnStdout: true).trim()
    def sec_vulns = parse(results)
    results = null
    sec_vulns.each{issue ->
        def title = issue["title"]
        def message = issue["description"]
        def component = issue["path"]
        def line = issue["line"]
        if (!vulns.containsKey(title)){
                vulns[title] = []
        }
        vulns[title].add([message, component, line])

    }
    
    sh 'rm output.json'
}

return this