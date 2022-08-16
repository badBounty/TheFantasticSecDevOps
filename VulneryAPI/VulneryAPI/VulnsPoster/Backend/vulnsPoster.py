from VulnsPoster.models import *
from VulneryAPI.Backend import logger, elasticsearch
from VulneryAPI.settings import ALL, LOGGER_APP_NAMES, VULN_SEVERITY, VULN_STATUS, VULN_TYPES
from VulnsPoster.Backend import dataChecker

import json
import datetime 
import sys

#--------------Generic--------------#

#Recieves the vuln

def recieveVuln(request):
    try:
        vulnJSON = request.body
        return json.loads(vulnJSON.decode('utf-8'))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------SAST----------------#
        
#Process the SAST vuln for uploading/updating

def processSASTVuln(vulnJSON):
    try:
        if dataChecker.checkVulnSeverityAndStatusForPosting(vulnJSON['Severity'], vulnJSON['Status']):
            vulnObject = VulnSAST("",
            vulnJSON['Title'].replace('\\',"/"), 
            vulnJSON['Description'].replace('\\',"/"),
            vulnJSON['Component'], 
            vulnJSON['Line'], 
            vulnJSON['Affected_code'].replace('\\',"/"), 
            vulnJSON['Commit'],
            vulnJSON['Username'],
            vulnJSON['Pipeline_name'],
            vulnJSON['Branch'],
            vulnJSON['Language'],
            vulnJSON['Hash'],
            vulnJSON['Severity'],
            datetime.datetime.now().strftime("%Y-%m-%d"), #This format is for elastic (yyyy-MM-dd)
            vulnJSON['Recommendation'],
            vulnJSON['Status']
            )
            return vulnObject
        return None #Return exception object cannot be posted. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets a SAST List of Objects for full listing

def getSASTListObjectsForFullListing(SASTData):
    try:
        SASTVulnsList = []
        for vulnSAST in SASTData:
            vulnObject = VulnSAST(str(vulnSAST['_id']),
                    str(vulnSAST['_source']['vuln_title']),
                    str(vulnSAST['_source']['vuln_description']),
                    str(vulnSAST['_source']['vuln_component']),
                    None,
                    None,
                    None,
                    None,
                    str(vulnSAST['_source']['vuln_pipeline']),
                    None,
                    None,
                    None,
                    str(vulnSAST['_source']['vuln_severity']), 
                    str(vulnSAST['_source']['vuln_date']), #Convert into datetime. TODO
                    None,
                    str(vulnSAST['_source']['vuln_status']) 
                ) 
            SASTVulnsList.append(vulnObject)
        return SASTVulnsList
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
   
#Uploads the SAST vuln to Elastic

def uploadSASTVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.uploadSASTVuln(vulnObject)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Updates the SAST vuln to Elastic

def updateSASTVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.updateSASTVuln(vulnObject)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Deletes the SAST vuln

def deleteSASTVulnFromElastic(vulnSASTID):
    try:
        return elasticsearch.deleteSASTVuln(vulnSASTID)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all SAST vulns from Elastic

def getAllSASTVulnsFromElastic():
    try:
        recievedSASTVulns = elasticsearch.getAllSASTVulnsFromElastic()['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulns), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all SAST vulns by Date from Elastic

def getAllSASTVulnsByDateFromElastic(fromDate, toDate):
    try:
        recievedSASTVulnsByDate = elasticsearch.getAllSASTVulnsByDateFromElastic(fromDate, toDate)['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulnsByDate), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())    

#Gets all SAST vulns by Severity from Elastic

def getAllSASTVulnsBySeverityFromElastic(severity):
    try:
        recievedSASTVulnsBySeverity = elasticsearch.getSASTVulnsBySeverity(severity)['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulnsBySeverity), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())  

#Gets all SAST vulns by Severity and Date from Elastic

def getAllSASTVulnsBySeverityDateFromElastic(severity, fromDate, toDate):
    try:
        recievedSASTVulnsBySeverityDate = elasticsearch.getSASTVulnsBySeverityDate(severity, fromDate, toDate)['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulnsBySeverityDate), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all SAST vulns by Status from Elastic

def getAllSASTVulnsByStatusFromElastic(status):
    try:
        recievedSASTVulnsByStatus = elasticsearch.getSASTVulnsByStatus(status)['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulnsByStatus), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all SAST vulns by Status and Date from Elastic

def getAllSASTVulnsByStatusDateFromElastic(status, fromDate, toDate):
    try:
        recievedSASTVulnsByStatusDate = elasticsearch.getSASTVulnsByStatusDate(status, fromDate, toDate)['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulnsByStatusDate), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all SAST vulns by Severity and Status from Elastic

def getAllSASTVulnsBySeverityStatusFromElastic(severity, status):
    try:
        recievedSASTVulnsBySeverityStatus = elasticsearch.getSASTVulnsBySeverityStatus(severity, status)['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulnsBySeverityStatus), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all SAST vulns by Severity, Status and Date from Elastic

def getAllSASTVulnsBySeverityStatusDateFromElastic(severity, status, fromDate, toDate):
    try:
        recievedSASTVulnsBySeverityStatusDate = elasticsearch.getSASTVulnsBySeverityStatusDate(severity, status, fromDate, toDate)['hits']['hits']
        SASTVulnsJSON = json.dumps(getSASTListObjectsForFullListing(recievedSASTVulnsBySeverityStatusDate), cls=Encoder)
        return SASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets SAST vuln by ID from Elastic

def getSASTVulnByIDFromElastic(vulnID):
    try:
        recievedSASTVuln = elasticsearch.getSASTVulnByIDFromElastic(vulnID)['hits']['hits'][0]
        vulnSASTObject = VulnSAST(str(recievedSASTVuln['_id']),
                    str(recievedSASTVuln['_source']['vuln_title']),
                    str(recievedSASTVuln['_source']['vuln_description']),
                    str(recievedSASTVuln['_source']['vuln_component']),
                    str(recievedSASTVuln['_source']['vuln_line']),
                    str(recievedSASTVuln['_source']['vuln_affected_code']),
                    str(recievedSASTVuln['_source']['vuln_commit']),
                    str(recievedSASTVuln['_source']['vuln_username']),
                    str(recievedSASTVuln['_source']['vuln_pipeline']),
                    str(recievedSASTVuln['_source']['vuln_branch']),
                    str(recievedSASTVuln['_source']['vuln_language']),
                    str(recievedSASTVuln['_source']['vuln_hash']), #Don't bring Hash. TODO
                    str(recievedSASTVuln['_source']['vuln_severity']), 
                    str(recievedSASTVuln['_source']['vuln_date']), #Convert into datetime. TODO
                    str(recievedSASTVuln['_source']['vuln_recommendation']),
                    str(recievedSASTVuln['_source']['vuln_status']) 
                ) 
        SASTVulnJSON = json.dumps(vulnSASTObject, cls=Encoder)
        return SASTVulnJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------DAST----------------#

#Process the DAST vuln for uploading/updating

def processDASTVuln(vulnJSON):
    try:    
        if dataChecker.checkVulnSeverityAndStatusForPosting(vulnJSON['Severity'], vulnJSON['Status']):
            vulnObject = VulnDAST(None, 
                vulnJSON['Title'].replace('\\',"/"),
                vulnJSON['Description'].replace('\\',"/"),
                vulnJSON['Affected_resource'],
                vulnJSON['Affected_urls'], 
                vulnJSON['Recommendation'],
                vulnJSON['Severity'],
                datetime.datetime.now().strftime("%Y-%m-%d"), #This format is for elastic (yyyy-MM-dd)
                vulnJSON['Status']
            )
            return vulnObject
        return None #Return exception. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets a DAST object for full listing

def getDASTListObjectsForFullListing(DASTData):
    try:
        DASTVulnsList = []
        for vulnDAST in DASTData:
            vulnObject = VulnDAST(str(vulnDAST['_id']),
                    str(vulnDAST['_source']['vuln_title']),
                    None,
                    str(vulnDAST['_source']['vuln_affected_resource']),
                    str(vulnDAST['_source']['vuln_affected_urls']),
                    None,
                    str(vulnDAST['_source']['vuln_severity']),
                    str(vulnDAST['_source']['vuln_date']), #Convert into datetime. TODO
                    str(vulnDAST['_source']['vuln_status']) 
                ) 
            DASTVulnsList.append(vulnObject)
        return DASTVulnsList
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Uploads the DAST vuln to Elastic

def uploadDASTVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.uploadDASTVuln(vulnObject)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Updates the DAST vuln to Elastic

def updateDASTVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.updateDASTVuln(vulnObject)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Deletes the DAST vuln

def deleteDASTVulnFromElastic(vulnDASTID):
    try:
        return elasticsearch.deleteDASTVuln(vulnDASTID)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all DAST vulns from Elastic

def getAllDASTVulnsFromElastic():
    try:
        recievedDASTVulns = elasticsearch.getAllDASTVulnsFromElastic()['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulns), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all SAST vulns by Date from Elastic

def getAllDASTVulnsByDateFromElastic(fromDate, toDate):
    try:
        recievedDASTVulnsByDate = elasticsearch.getAllDASTVulnsByDateFromElastic(fromDate, toDate)['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulnsByDate), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())   

#Gets all DAST vulns by Severity from Elastic

def getAllDASTVulnsBySeverityFromElastic(severity):
    try:
        recievedDASTVulnsBySeverity = elasticsearch.getDASTVulnsBySeverity(severity)['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulnsBySeverity), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())  

#Gets all DAST vulns by Severity and Date from Elastic

def getAllDASTVulnsBySeverityDateFromElastic(severity, fromDate, toDate):
    try:
        recievedDASTVulnsBySeverityDate = elasticsearch.getDASTVulnsBySeverityDate(severity, fromDate, toDate)['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulnsBySeverityDate), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all DAST vulns by Status from Elastic

def getAllDASTVulnsByStatusFromElastic(status):
    try:
        recievedDASTVulnsByStatus = elasticsearch.getDASTVulnsByStatus(status)['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulnsByStatus), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all DAST vulns by Status and Date from Elastic

def getAllDASTVulnsByStatusDateFromElastic(status, fromDate, toDate):
    try:
        recievedDASTVulnsByStatusDate = elasticsearch.getDASTVulnsByStatusDate(status, fromDate, toDate)['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulnsByStatusDate), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all DAST vulns by Severity and Status from Elastic

def getAllDASTVulnsBySeverityStatusFromElastic(severity, status):
    try:
        recievedDASTVulnsBySeverityStatus = elasticsearch.getDASTVulnsBySeverityStatus(severity, status)['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulnsBySeverityStatus), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all DAST vulns by Severity, Status and Date from Elastic

def getAllDASTVulnsBySeverityStatusDateFromElastic(severity, status, fromDate, toDate):
    try:
        recievedDASTVulnsBySeverityStatusDate = elasticsearch.getDASTVulnsBySeverityStatusDate(severity, status, fromDate, toDate)['hits']['hits']
        DASTVulnsJSON = json.dumps(getDASTListObjectsForFullListing(recievedDASTVulnsBySeverityStatusDate), cls=Encoder)
        return DASTVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())         

#Gets DAST vuln by ID from Elastic

def getDASTVulnByIDFromElastic(vulnID):
    try:
        recievedDASTVuln = elasticsearch.getDASTVulnByIDFromElastic(vulnID)['hits']['hits'][0]
        vulnDASTObject = VulnDAST(str(recievedDASTVuln['_id']),
                    str(recievedDASTVuln['_source']['vuln_title']),
                    str(recievedDASTVuln['_source']['vuln_description']),
                    str(recievedDASTVuln['_source']['vuln_affected_resource']),
                    str(recievedDASTVuln['_source']['vuln_affected_urls']),
                    str(recievedDASTVuln['_source']['vuln_recommendation']),
                    str(recievedDASTVuln['_source']['vuln_severity']),
                    str(recievedDASTVuln['_source']['vuln_date']), #Convert into datetime. TODO
                    str(recievedDASTVuln['_source']['vuln_status'])
                ) 
        DASTVulnJSON = json.dumps(vulnDASTObject, cls=Encoder)
        return DASTVulnJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------Infra----------------#
        
#Process the Infra vuln for uploading/updating

def processInfraVuln(vulnJSON):
    try:
        if dataChecker.checkVulnSeverityAndStatusForPosting(vulnJSON['Severity'], vulnJSON['Status']):
            vulnObject = VulnInfra("",
            vulnJSON['Title'].replace('\\',"/"), 
            vulnJSON['Description'].replace('\\',"/"),
            vulnJSON['Observation'].replace('\\',"/"), 
            vulnJSON['Domain'], 
            vulnJSON['Subdomain'],
            vulnJSON['Extra_info'].replace('\\',"/"),
            vulnJSON['CVSS_Score'],
            vulnJSON['Language'],
            vulnJSON['Severity'],
            vulnJSON['Recommendation'],
            datetime.datetime.strptime(vulnJSON['Date'],"%Y-%m-%d").date(),
            vulnJSON['Status']
            )
            return vulnObject
        return None #Return exception object cannot be posted. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets a Infra List of Objects for full listing

def getInfraListObjectsForFullListing(InfraData):
    try:
        InfraVulnsList = []
        for vulnInfra in InfraData:
            vulnObject = VulnInfra(str(vulnInfra['_id']),
                    str(vulnInfra['_source']['vuln_title']),
                    str(vulnInfra['_source']['vuln_description']),
                    None,
                    str(vulnInfra['_source']['vuln_domain']),
                    None,
                    None,
                    None,
                    None,
                    str(vulnInfra['_source']['vuln_severity']), 
                    None,
                    str(vulnInfra['_source']['vuln_date']), #Convert into datetime. TODO
                    str(vulnInfra['_source']['vuln_status']) 
                ) 
            InfraVulnsList.append(vulnObject)
        return InfraVulnsList
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
   
#Uploads the Infra vuln to Elastic

def uploadInfraVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.uploadInfraVuln(vulnObject)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Updates the Infra vuln to Elastic

def updateInfraVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.updateInfraVuln(vulnObject)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Deletes the Infra vuln

def deleteInfraVulnFromElastic(vulnInfraID):
    try:
        return elasticsearch.deleteInfraVuln(vulnInfraID)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all Infra vulns from Elastic

def getAllInfraVulnsFromElastic():
    try:
        recievedInfraVulns = elasticsearch.getAllInfraVulnsFromElastic()['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulns), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all Infra vulns by Date from Elastic

def getAllInfraVulnsByDateFromElastic(fromDate, toDate):
    try:
        recievedInfraVulnsByDate = elasticsearch.getAllInfraVulnsByDateFromElastic(fromDate, toDate)['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulnsByDate), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())    

#Gets all Infra vulns by Severity from Elastic

def getAllInfraVulnsBySeverityFromElastic(severity):
    try:
        recievedInfraVulnsBySeverity = elasticsearch.getInfraVulnsBySeverity(severity)['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulnsBySeverity), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())  

#Gets all Infra vulns by Severity and Date from Elastic

def getAllInfraVulnsBySeverityDateFromElastic(severity, fromDate, toDate):
    try:
        recievedInfraVulnsBySeverityDate = elasticsearch.getInfraVulnsBySeverityDate(severity, fromDate, toDate)['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulnsBySeverityDate), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all Infra vulns by Status from Elastic

def getAllInfraVulnsByStatusFromElastic(status):
    try:
        recievedInfraVulnsByStatus = elasticsearch.getInfraVulnsByStatus(status)['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulnsByStatus), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all Infra vulns by Status and Date from Elastic

def getAllInfraVulnsByStatusDateFromElastic(status, fromDate, toDate):
    try:
        recievedInfraVulnsByStatusDate = elasticsearch.getInfraVulnsByStatusDate(status, fromDate, toDate)['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulnsByStatusDate), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all Infra vulns by Severity and Status from Elastic

def getAllInfraVulnsBySeverityStatusFromElastic(severity, status):
    try:
        recievedInfraVulnsBySeverityStatus = elasticsearch.getInfraVulnsBySeverityStatus(severity, status)['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulnsBySeverityStatus), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets all Infra vulns by Severity, Status and Date from Elastic

def getAllInfraVulnsBySeverityStatusDateFromElastic(severity, status, fromDate, toDate):
    try:
        recievedInfraVulnsBySeverityStatusDate = elasticsearch.getInfraVulnsBySeverityStatusDate(severity, status, fromDate, toDate)['hits']['hits']
        InfraVulnsJSON = json.dumps(getInfraListObjectsForFullListing(recievedInfraVulnsBySeverityStatusDate), cls=Encoder)
        return InfraVulnsJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info()) 

#Gets Infra vuln by ID from Elastic

def getInfraVulnByIDFromElastic(vulnID):
    try:
        recievedInfraVuln = elasticsearch.getInfraVulnByIDFromElastic(vulnID)['hits']['hits'][0]
        vulnInfraObject = VulnInfra(str(recievedInfraVuln['_id']),
                    str(recievedInfraVuln['_source']['vuln_title']),
                    str(recievedInfraVuln['_source']['vuln_description']),
                    str(recievedInfraVuln['_source']['vuln_observation']),
                    str(recievedInfraVuln['_source']['vuln_domain']),
                    str(recievedInfraVuln['_source']['vuln_subdomain']),
                    str(recievedInfraVuln['_source']['vuln_extra_info']),
                    str(recievedInfraVuln['_source']['vuln_cvss_score']),
                    str(recievedInfraVuln['_source']['vuln_language']),
                    str(recievedInfraVuln['_source']['vuln_severity']), 
                    str(recievedInfraVuln['_source']['vuln_recommendation']),
                    str(recievedInfraVuln['_source']['vuln_date']), #Convert into datetime. TODO
                    str(recievedInfraVuln['_source']['vuln_status']) 
                ) 
        InfraVulnJSON = json.dumps(vulnInfraObject, cls=Encoder)
        return InfraVulnJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#-------------Severity---------------#   

#Gets all severities 

def getAllSeverities():
    try:
        severityList = []
        severityList.append(Severity(str(ALL)))
        for severity in VULN_SEVERITY:
            try:
                severityObject = Severity(str(severity['SEV']))
                severityList.append(severityObject)
            except:
                logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())  
        severitiesJSON = json.dumps(severityList,cls=Encoder)
        return severitiesJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#--------------Status---------------#   

#Gets all status from Settings.

def getAllStatus():
    try:
        statusList = []
        statusList.append(Status(str(ALL))) 
        for status in VULN_STATUS:
            try:
                statusObject = Status(str(status['STATUS']))
                statusList.append(statusObject)
            except:
                logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())        
        statusJSON = json.dumps(statusList, cls=Encoder)
        return statusJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------Type----------------#  

#Gets all the vuln types from Settings.

def getVulnsTypes():
    try:
        vulnTypeList = []
        for vulnType in VULN_TYPES:
            try:
                vulnTypeObject = VulnType(str(VULN_TYPES[vulnType]))
                vulnTypeList.append(vulnTypeObject)
            except:
                logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        vulnTypeJSON = json.dumps(vulnTypeList, cls=Encoder)
        return vulnTypeJSON
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())