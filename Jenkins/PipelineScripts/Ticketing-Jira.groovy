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

### Ticketing
Script que permite crar tickets para cada vul que se le pase.

#### Interfaz

##### init(strategy) 
Requiere un strategy, que puede ser Redmine o Jira, el cual expona el meotodo "createIssue" 
```groovy
def createIssue(def keyProject, def ruleName, def issueMessage, def affectedResource, def affectedLine, def siteJira)
```
##### runStage(site, keyProject, vulsJsonList) 
Metodo principal para acceder a la api, permite la creacion de un issue por cada vulnerabilidad que se itere. Requiere llamar a init para configurar el strategy, el site, el "key project" y una coleccion de vulnerabilidades a subir en formato:
```JSON
{
	VulnRuleName : [IssueMessage,AffectedResource,AffectedLine]
}
```
##### getIssues()
Devuelve un diccionario (key-value) con los issues creados el siguiente formato:
```JSON
{
	Id : [KeyProject,Type,Summary,Description,VulnRuleName, UrlIssue]
}
```

/*
Example of call in pipeline:
        stage('Ticketing'){
            steps{
                script{
                    modules.nineth.runStage('team-1588778856415.atlassian.net', 'JENKTEST', vulsJsonList)
                }
            }
        }
*/