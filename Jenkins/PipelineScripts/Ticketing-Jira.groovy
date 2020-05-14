new issues = [:]

def runStage(){

    def keyProject = "KEY"
    def summary = "SUMMARY"
    def description ="DESCP"
    def issueType = "Bug"
    def newIssue = [fields: [ project: [key: keyProject],
                              summary: summary,
                              description: description,
                              issuetype: [name: issueType]
                             ]

    def response = jiraNewIssue issue: newIssue
    echo response.successful.toString()
    echo response.data.toString()
}


def getNewIssues(){
	return issues
}

return this