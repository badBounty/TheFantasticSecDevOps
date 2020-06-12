import groovy.json.JsonSlurperClassic
vulns = [:]

def runStage(){
    def pc = 1
    def ps = 100
    def total = 200
    while ((pc * ps) < total){
        def response = httpRequest "http://192.168.0.19:9000/api/issues/search?p=${pc}&ps=${ps}"
        print(response.status)
        def json = new JsonSlurperClassic().parseText(response.content)
        
        
        json.issues.each{issue ->
            if (issue.type == 'VULNERABILITY' & issue.status == 'OPEN'){
                if (!vulns.containsKey(issue.rule)){
                    vulns[issue.rule] = []
                }
                def message = issue.message.replaceAll('"', "'")
                def component = issue.component
                def line = issue.line
                def date = issue.updateDate.split('T')[0]
                def data = """{
                    "Component": "$component",
                    "Line": $line,
                    "Message": "$message",
                    "Date": "$date"
                }"""
                print(data)
                def res = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: data, url: 'http://192.168.0.23:5000/api/issue'
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

