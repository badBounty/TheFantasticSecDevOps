import groovy.json.JsonSlurperClassic

def runStage(){
    try {
        sshagent(['ssh-key']) {
            def projname = env.JOB_NAME
            sh 'ls'
            //sh 'ssh-keygen -f "/var/jenkins_home/.ssh/known_hosts" -R [192.168.0.23]:44022'
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 rm -rf /home/${projname}"
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 ls /home/"
            sh 'scp -P 44022 -o StrictHostKeyChecking=no -r $(pwd) root@192.168.0.23:/home'
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 nodejsscan -d /home/${projname} -o /home/output.json"
            sh "scp -P 44022 -o StrictHostKeyChecking=no root@192.168.0.23:/home/output.json ./output.json"
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 rm /home/output.json"
            sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 rm -rf /home/${projname}/*"
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
    print(vulns)
    sh 'rm output.json'
}

return this