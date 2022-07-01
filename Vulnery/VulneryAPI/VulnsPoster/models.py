from asyncio.windows_events import NULL
from django.db import models

import datetime

#VulnSAST model

class VulnSAST(models.Model):
    vulnID = models.IntegerField(blank=True)
    Title = models.CharField(max_length=150, null=True)
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
    Severity = models.CharField(max_length=10) #Add severity class
    Date = models.DateTimeField() #Add date within posting

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def getVuln(self):
        return self

    @classmethod
    def createVuln(self, vulnJSON):
        return VulnSAST(vulnJSON)

    def __init__(self, vulnJSON):
        self.vulnID = NULL #Add with elastic
        self.Title = str(vulnJSON['Title'])
        self.Description = str(vulnJSON['Description'])
        self.Component = vulnJSON['Component']
        self.Line = int(vulnJSON['Line'])
        self.AffectedCode = vulnJSON['Affected_code']
        self.Commit = vulnJSON['Commit']
        self.Username = vulnJSON['Username']
        self.Pipeline = vulnJSON['Pipeline_name']
        self.Branch = vulnJSON['Branch']
        self.Language = vulnJSON['Language']
        self.Hash = vulnJSON['Hash']
        self.Severity = vulnJSON['Severity_tool'] #create severity class and search in elastic. ID must be stored.
        self.Date = datetime.datetime.now().strftime("%d/%m/%Y")

#VulnDAST model

class VulnDAST(models.Model):
    vulnID = models.IntegerField(blank=True)
    Title = models.CharField(max_length=150, null=True)
    Description = models.CharField(max_length=500, null=True)
    Component = models.CharField(max_length=100, null=True)
    AffectedURLs = models.CharField(max_length=5000, null=True)
    Recommendation = models.CharField(max_length=200, null=True)
    Severity = models.CharField(max_length=10) #Add severity class

    class Meta:
        app_label = 'VulnsPoster'

    @classmethod
    def getVuln(self):
        return self

    @classmethod
    def createVuln(self, vulnJSON):
        return VulnDAST(vulnJSON)

    def __init__(self, vulnJSON):
        self.vulnID = NULL #Add with elastic
        self.Title = str(vulnJSON['Title'])
        self.Description = str(vulnJSON['Description'])
        self.Component = vulnJSON['Component']
        self.Line = int(vulnJSON['Line'])
        self.AffectedURLs = vulnJSON['Affected_URLs']
        self.Recommendation = vulnJSON['Recommendation']
        self.Severity = vulnJSON['Severity_tool'] #create severity class and search in elastic. ID must be stored.

#Severity model

class Severity(models.Model):
    severityID = models.IntegerField()
    severity = models.CharField(max_length=10)

    class Meta:
        app_label = 'VulnsPoster'

    #Get ID by string recieved in elastic and store the ID in Vuln model.

