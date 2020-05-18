//https://www.jenkins.io/doc/pipeline/steps/jira-steps/
def createIssue(def keyProject, def ruleName, def issueMessage, def affectedResource, def affectedLine, def siteJira){
    def summary = ruleName
    def description = 'Decription: ' + issueMessage + '\n Affected resource: '+ affectedResource + ' \n affected line: ' + affectedLine
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

    def result = [:]
    result[id] = []
    result[id].add([keyProject, issueTypeBug, summary, description, ruleName, url])
}

return this