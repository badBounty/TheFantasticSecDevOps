import groovy.json.JsonSlurperClassic
vulns = [:]

def runStage(){
    def pc = 1
    def ps = 100
    def total = 200
    while ((pc * ps) < total){
        def response = httpRequest "http://${env.SASTIP}:${env.sonarport}/api/issues/search?p=${pc}&ps=${ps}"
        print(response.status)
        def json = new JsonSlurperClassic().parseText(response.content)
        
        
        json.issues.each{issue ->
            if (issue.type == 'VULNERABILITY' & issue.status == 'OPEN'){
                if (!vulns.containsKey(issue.rule)){
                    vulns[issue.rule] = []
                }
                def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').take(7)
                def message = issue.message.replaceAll('"', "'")
                def component = issue.component
                def line = issue.line
                def date = issue.updateDate.split('T')[0]
                def data = """{
                    "Component": "$component",
                    "Line": $line,
                    "Message": "$message",
                    "Date": "$date",
                    "Commit": "$GIT_COMMIT"
                }"""
                print(data)
                def res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: "http://${env.dashboardIP}/add_code_vulnerability/"
                println(res.content)
                vulns[issue.rule].add([message, component, line])
                sleep(3)
            }
        }
        total = json.total
        pc += 1
    }

}


def getVulnerabilities(){
	return vulns
}

return this

