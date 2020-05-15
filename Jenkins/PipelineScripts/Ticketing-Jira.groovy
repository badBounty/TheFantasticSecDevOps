issues = [:]

def runStage(def siteJira, def keyProject, def vulsJsonList){
    for(vul in vulsJsonList){
        def vulnRule = vul.key
        def issueMessage= vul.value[0]
        def affectedResource = vul.value[1]
        def affectedLine = vul.value[2]
        createIssue(keyProject, vulnRule, issueMessage, affectedResource, affectedLine, siteJira)
    }
}

def createIssue(def keyProject, def ruleName, def issueMessage, def affectedResource, def affectedLine, def siteJira){
    def keyProject = keyProject
    def summary = ruleName + ':' + issueMessage
    def description = 'Affected resource: affectedResource \n affected line: affectedLine'
    def issueTypeBug = 'Bug'

    def newIssue = [fields: [ 
                            project: [key: keyProject],
                            summary: summary,
                            description: description,
                            issuetype: [name: issueTypeBug]
                    ]]

    def response = jiraNewIssue issue: newIssue, site: siteJira
    def url = response.data.self
    def id  = url.split('/').last();

    vulns[id] = []
    vulns[id].add([keyProject, issueType, summary, description, ruleName, url])
}

def getNewIssues(){
	return issues
}

return this