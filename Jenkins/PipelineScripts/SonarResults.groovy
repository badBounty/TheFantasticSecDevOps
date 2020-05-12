import groovy.json.JsonSlurper
vulns = [:]

def runStage(){
    def pc = 1
    def ps = 100
    def total = 200
    while ((pc * ps) < total){
        def response = httpRequest "http://192.168.0.21:9000/api/issues/search?p=${pc}&ps=${ps}"
        def json = new JsonSlurper().parseText(response.content)
                
        json.issues.each{issue ->
            if (issue.type == 'VULNERABILITY'){
                if (!vulns.containsKey(issue.rule)){
                    vulns[issue.rule] = []
                }
                vulns[issue.rule].add([issue.message, issue.component, issue.line])
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
