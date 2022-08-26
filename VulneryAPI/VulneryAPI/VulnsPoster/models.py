from json import JSONEncoder

#VulnType class

class VulnType():

    class Meta:
        app_label = 'VulnsPoster'

    def __init__(self, vulnType):
        self.vulnType = vulnType    

#VulnSAST class

class VulnSAST():

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def getVuln(self):
        return self

    def __init__(self, vulnID, vulnTitle, vulnDescription, vulnComponent, vulnLine, vulnAffectedCode, vulnCommit,
    vulnUsername, vulnPipeline, vulnBranch, vulnLanguage, vulnHash, vulnSeverity, vulnDate, vulnRecommendation, vulnStatus):
        self.vulnID = vulnID
        self.Title = vulnTitle
        self.Description = vulnDescription
        self.Component = vulnComponent
        self.Line = vulnLine
        self.AffectedCode = vulnAffectedCode
        self.Commit = vulnCommit
        self.Username = vulnUsername
        self.Pipeline = vulnPipeline
        self.Branch = vulnBranch
        self.Language = vulnLanguage
        self.Hash = vulnHash
        self.Severity = vulnSeverity
        self.Date = vulnDate
        self.Recommendation = vulnRecommendation
        self.Status = vulnStatus
        
#VulnDAST class

class VulnDAST():

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def getVuln(self):
        return self

    def __init__(self, vulnID, vulnTitle, vulnDescription, vulnAffectedResource, vulnAffectedURLs, vulnRecommendation,
    vulnSeverity, vulnDate, vulnStatus):
        self.vulnID = vulnID
        self.Title = vulnTitle
        self.Description = vulnDescription
        self.AffectedResource = vulnAffectedResource
        self.AffectedURLs = vulnAffectedURLs
        self.Recommendation = vulnRecommendation
        self.Severity = vulnSeverity
        self.Date = vulnDate
        self.Status = vulnStatus

#VulnInfra class

class VulnInfra():
    
    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def getVuln(self):
        return self
    
    def __init__(self, vulnID, vulnTitle, vulnDescription, vulnObservation, vulnDomain, vulnSubdomain, vulnExtraInfo, vulnCVSS_Score, 
    vulnLanguage, vulnSeverity, vulnRecommendation, vulnDate, vulnStatus):
        self.vulnID = vulnID
        self.Title = vulnTitle
        self.Description = vulnDescription
        self.Observation = vulnObservation
        self.Domain = vulnDomain
        self.Subdomain = vulnSubdomain
        self.ExtraInfo = vulnExtraInfo
        self.CVSS_Score = vulnCVSS_Score
        self.Language = vulnLanguage
        self.Severity = vulnSeverity
        self.Recommendation = vulnRecommendation
        self.Date = vulnDate
        self.Status = vulnStatus

#Severity class

class Severity():

    class Meta:
        app_label = 'VulnsPoster'

    def __init__(self, severityName):
        self.severity = severityName

#Status class

class Status():

    class Meta:
        app_label = 'VulnsPoster'

    def __init__(self, statusName):
        self.status = statusName

#JSON Encoder class

class Encoder(JSONEncoder):
    
    def default(self, o):
        return o.__dict__