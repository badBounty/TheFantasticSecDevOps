issues = [:]
def strategy = null

def Init(def strategySetup){
    strategy = strategySetup
}

def runStage(def site, def keyProject, def vulsJsonList){
    for(vul in vulsJsonList){

        def vulnRule = vul.key
        def issueMessage= vul.value[0]
        def affectedResource = vul.value[1]
        def affectedLine = vul.value[2]
        
        def idIssue = strategy.createIssue(keyProject, vulnRule, issueMessage, affectedResource, affectedLine, siteJira)
        
        issues[idIssue.first()] = []
        issues[idIssue.first()].add(idIssue.last())
    }
}

def getNewIssues(){
	return issues
}

return this