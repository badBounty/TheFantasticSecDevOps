new issues = [:]

def runStage(){
    def keyProject = "KEY"
    def summary = "SUMMARY"
    def description ="DESCP"
    def issueType = "Bug"
    def newIssue = [fields: [ project: [key: keyProject],
                              summary: summary,
                              description: description,
                              issuetype: issueType]

    def response = jiraNewIssue issue: newIssue
    echo response.successful.toString()
    echo response.data.toString()
}


def getNewIssues(){
	return issue [fields: newFields]
}

return this