from django.db import models

from datetime import datetime
import datetime as datetm

#VulnSAST model

class VulnSAST(models.Model):
    vulnID = models.CharField(max_length=50)
    Title = models.CharField(max_length=300, null=True)
    Description = models.CharField(max_length=500, null=True)
    Component = models.CharField(max_length=100, null=True)
    Line = models.IntegerField(max_length=10, null=True)
    AffectedCode = models.CharField(max_length=5000, null=True)
    Commit = models.CharField(max_length=200, null=True)
    Username = models.CharField(max_length=100, null=True)
    Pipeline = models.CharField(max_length=100, null=True)
    Branch = models.CharField(max_length=50, null=True)
    Language = models.CharField(max_length=3, null=True)
    Hash = models.CharField(null=True)
    Severity = models.CharField(max_length=10) 
    Recommendation = models.CharField(max_length=200, null=True) #Set manually after uploading
    Date = models.DateTimeField() 
    Status = models.CharField(max_length=20) #Add Status class

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def getVuln(self):
        return self

    @classmethod
    def createVuln(self, vulnJSON):
        return VulnSAST(vulnJSON)

    def __init__(self, vulnJSON):
        self.Title = vulnJSON['Title'].replace('\\',"/")
        self.Description = vulnJSON['Description'].replace('\\',"/")
        self.Component = vulnJSON['Component']
        self.Line = int(vulnJSON['Line'])
        self.AffectedCode = vulnJSON['Affected_code'].replace('\\',"/")
        self.Commit = vulnJSON['Commit']
        self.Username = vulnJSON['Username']
        self.Pipeline = vulnJSON['Pipeline_name']
        self.Branch = vulnJSON['Branch']
        self.Language = vulnJSON['Language']
        self.Hash = vulnJSON['Hash']
        self.Severity = vulnJSON['Severity_tool'] 
        self.Date = datetm.datetime.now().strftime("%Y-%m-%d") #This format is for elastic (yyyy-MM-dd)
        self.Recommendation = "" #Set manually after uploading
        self.Status = "open"

#VulnDAST model

class VulnDAST(models.Model):
    vulnID = models.CharField(max_length=50)
    Title = models.CharField(max_length=150, null=True)
    Description = models.CharField(max_length=500, null=True)
    AffectedResource = models.CharField(max_length=500, null=True)
    AffectedURLs = models.CharField(max_length=5000, null=True)
    Recommendation = models.CharField(max_length=200, null=True)
    Severity = models.CharField(max_length=10) 
    Date = models.DateTimeField() 
    Status = models.CharField(max_length=20) #Add Status class

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def getVuln(self):
        return self

    @classmethod
    def createVuln(self, vulnJSON):
        return VulnDAST(vulnJSON)

    def __init__(self, vulnJSON):
        self.Title = vulnJSON['Title'].replace('\\',"/")
        self.Description = vulnJSON['Description'].replace('\\',"/")
        self.AffectedResource = vulnJSON['Affected_resource']
        self.AffectedURLs = vulnJSON['Affected_urls']
        self.Recommendation = vulnJSON['Recommendation']
        self.Severity = vulnJSON['Severity_tool'] 
        self.Date = datetime.strptime(vulnJSON['Date'],"%Y-%m-%d").date() #This format is for elastic (yyyy-MM-dd)
        self.Status = "open"

#Severity model

class Severity(models.Model):
    severityID = models.CharField(max_length=50)
    severity = models.CharField(max_length=12)

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def createSeverity(self, severityID, severityName):
        return Severity(severityID, severityName)

    def __init__(self, severityID, severityName):
        self.severityID = severityID
        self.severity = severityName

#Status model

class Status(models.Model):
    statusID = models.CharField(max_length=50)
    status = models.CharField(max_length=20)

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def createStatus(self, statusID, statusName):
        return Status(statusID, statusName)

    def __init__(self, statusID, statusName):
        self.statusID = statusID
        self.status = statusName
