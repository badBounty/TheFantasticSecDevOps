import groovy.json.JsonSlurper

issues = [:]

def runStage(def vulsJsonList, def keyProject){
    for(vul in vulsJsonList){
        def vulnRule = vul.key
            def issueMessage= vul.value[0]
            def affectedResource = vul.value[1]
            def affectedLine = vul.value[2]
    }
}

def createIssue(def, keyProject, def ruleName, def issueMessage, def affectedResource, def affectedLine){
    def keyProject = keyProject
    def summary = ruleName + ':' + issueMessage
    def description = 'Affected resource: affectedResource \n affected line: affectedLine'
    def issueType = "bug"

    def newIssue = [fields: [ 
                            project: [key: keyProject],
                            summary: summary,
                            description: description,
                            issuetype: [name: issueType]
                    ]]

    def response = jiraNewIssue issue: newIssue
    def id = response.data.toString()
    vulns[id] = []
    vulns[id].add([keyProject, issueType, summary, description, ruleName])
}

def getNewIssues(){
	return issues
}

return this