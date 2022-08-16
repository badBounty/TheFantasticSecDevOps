import sys
from datetime import datetime
from elasticsearch import Elasticsearch
from VulneryAPI.settings import ELASTIC_SEARCH, LOGGER_APP_NAMES
from VulneryAPI.Backend import logger
from VulnsPoster.models import *

#--------------Generic--------------#

#Gets instance of Elastic

def getInstance():
    return Elasticsearch([f'http://{ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"]}:{ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"]}'],
     http_auth=(ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"], ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"]), verify_certs=False)

#---------------SAST----------------#

#Uploads SAST vuln into Elastic

def uploadSASTVuln(vulnSAST):
    try:
        elastic = getInstance()
        processedVulnSAST = convertSASTVulnObjectToElasticDoc(vulnSAST)
        elastic.index(index=getSASTIndex(),body=processedVulnSAST)
        return 
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Updates SAST vuln into Elastic

def updateSASTVuln(vulnSAST):
    try:
        elastic = getInstance()
        processedVulnSAST = convertSASTVulnObjectToElasticDoc(vulnSAST)
        elastic.index(index=getSASTIndex(),id=vulnSAST.vulnID,body=processedVulnSAST)
        return  
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Deletes SAST vuln

def deleteSASTVuln(vulnSASTID):
    try:
        elastic = getInstance()
        elastic.delete(index=getSASTIndex(), id=str(vulnSASTID))
        return 
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Converts SAST vuln object to Elastic Doc Format

def convertSASTVulnObjectToElasticDoc(vulnObject):
    try:
        processedVuln = {
            "vuln_title": vulnObject.Title,
            "vuln_description": vulnObject.Description,
            "vuln_component": vulnObject.Component,
            "vuln_line": vulnObject.Line,
            "vuln_affected_code": vulnObject.AffectedCode,
            "vuln_commit": vulnObject.Commit,
            "vuln_username": vulnObject.Username,
            "vuln_pipeline": vulnObject.Pipeline,
            "vuln_branch": vulnObject.Branch,
            "vuln_language": vulnObject.Language,
            "vuln_hash": vulnObject.Hash,
            "vuln_severity": vulnObject.Severity,
            "vuln_recommendation": vulnObject.Recommendation,
            "vuln_date": vulnObject.Date,
            "vuln_status": vulnObject.Status
        }   
        return processedVuln
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets all SAST vulns from Elastic 

def getAllSASTVulnsFromElastic():
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), query={"match_all":{}},sort={"vuln_date":{"order":"desc"}}, 
        size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns by Date from Elastic

def getAllSASTVulnsByDateFromElastic(fromDate, toDate):
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate), "lte":str(toDate)}}}]}}, size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns by Severity 

def getSASTVulnsBySeverity(severity):
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"match":{"vuln_severity":str(severity)}}]}}, size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns by Severity and Date

def getSASTVulnsBySeverityDate(severity, fromDate, toDate):
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_severity":str(severity)}}]}}]}}, size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns by Status

def getSASTVulnsByStatus(status):
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"match":{"vuln_status":str(status)}}]}}, size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns by Status and Date

def getSASTVulnsByStatusDate(status, fromDate, toDate):
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_status":str(status)}}]}}]}}, size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns by Severity and Status

def getSASTVulnsBySeverityStatus(severity, status):
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"match":{"vuln_status":str(status)}},
        {"match":{"vuln_severity":str(severity)}}]}}, size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns by Severity, Status and Date

def getSASTVulnsBySeverityStatusDate(severity, status, fromDate, toDate):
    try:
        elastic = getInstance()
        SASTVulns = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_status":str(status)}},{"match":{"vuln_severity":str(severity)}}]}}]}}, size=int(getSASTVulnCount()))
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vuln by ID from Elastic

def getSASTVulnByIDFromElastic(vulnID):
    try:
        elastic = getInstance()
        SASTVuln = elastic.search(index=getSASTIndex(), 
        query={"bool":{"must":[{"match":{"_id":str(vulnID)}}]}})
        return SASTVuln
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass    

#Gets SAST total vulns count

def getSASTVulnCount():
    try:
        elastic = getInstance()
        totalVulns=elastic.search(index=getSASTIndex())
        return totalVulns['hits']['total']['value']
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST index from settings

def getSASTIndex():
    return ELASTIC_SEARCH["ELASTIC_INDEX"]["sast_vulns"]

#---------------DAST----------------#

#Uploads DAST vuln into Elastic

def uploadDASTVuln(vulnDAST):
    try:
        elastic = getInstance()
        processedVulnDAST = convertDASTVulnObjectToElasticDoc(vulnDAST)
        elastic.index(index=getDASTIndex(),body=processedVulnDAST)
        return 
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Updates DAST vuln into Elastic

def updateDASTVuln(vulnDAST):
    try:
        elastic = getInstance()
        processedVulnDAST = convertDASTVulnObjectToElasticDoc(vulnDAST)
        elastic.index(index=getDASTIndex(),id=vulnDAST.vulnID,body=processedVulnDAST)
        return  
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass


#Deletes DAST vuln

def deleteDASTVuln(vulnDASTID):
    try:
        elastic = getInstance()
        elastic.delete(index=getDASTIndex(), id=str(vulnDASTID))
        return
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Converts DAST vuln object to Elastic Doc Format

def convertDASTVulnObjectToElasticDoc(vulnObject):
    try:
        processedVuln = {
            "vuln_title": vulnObject.Title,
            "vuln_description": vulnObject.Description,
            "vuln_affected_resource": vulnObject.AffectedResource,
            "vuln_affected_urls": vulnObject.AffectedURLs,
            "vuln_severity": vulnObject.Severity,
            "vuln_recommendation": vulnObject.Recommendation,
            "vuln_date": vulnObject.Date,
            "vuln_status": vulnObject.Status
        }   
        return processedVuln  
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets all DAST vulns from Elastic 

def getAllDASTVulnsFromElastic():
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), query={"match_all":{}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns by Date from Elastic

def getAllDASTVulnsByDateFromElastic(fromDate, toDate):
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate), "lte":str(toDate)}}}]}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns by Severity 

def getDASTVulnsBySeverity(severity):
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"match":{"vuln_severity":str(severity)}}]}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns by Severity and Date

def getDASTVulnsBySeverityDate(severity, fromDate, toDate):
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_severity":str(severity)}}]}}]}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns by Status

def getDASTVulnsByStatus(status):
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"match":{"vuln_status":str(status)}}]}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns by Status and Date

def getDASTVulnsByStatusDate(status, fromDate, toDate):
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_status":str(status)}}]}}]}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns by Severity and Status

def getDASTVulnsBySeverityStatus(severity, status):
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"match":{"vuln_status":str(status)}},
        {"match":{"vuln_severity":str(severity)}}]}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns by Severity, Status and Date

def getDASTVulnsBySeverityStatusDate(severity, status, fromDate, toDate):
    try:
        elastic = getInstance()
        DASTVulns = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_status":str(status)}},{"match":{"vuln_severity":str(severity)}}]}}]}}, size=int(getDASTVulnCount()))
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vuln by ID from Elastic

def getDASTVulnByIDFromElastic(vulnID):
    try:
        elastic = getInstance()
        DASTVuln = elastic.search(index=getDASTIndex(), 
        query={"bool":{"must":[{"match":{"_id":str(vulnID)}}]}})
        return DASTVuln
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST total vulns count

def getDASTVulnCount():
    try:
        elastic = getInstance()
        totalVulns=elastic.search(index=getDASTIndex())
        return totalVulns['hits']['total']['value']
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST index from settings

def getDASTIndex():
    return ELASTIC_SEARCH["ELASTIC_INDEX"]["dast_vulns"]

#---------------Infra----------------#

#Uploads Infra vuln into Elastic

def uploadInfraVuln(vulnInfra):
    try:
        elastic = getInstance()
        processedVulnInfra = convertInfraVulnObjectToElasticDoc(vulnInfra)
        elastic.index(index=getInfraIndex(),body=processedVulnInfra)
        return 
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Updates Infra vuln into Elastic

def updateInfraVuln(vulnInfra):
    try:
        elastic = getInstance()
        processedVulnInfra = convertInfraVulnObjectToElasticDoc(vulnInfra)
        elastic.index(index=getInfraIndex(),id=vulnInfra.vulnID,body=processedVulnInfra)
        return  
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Deletes Infra vuln

def deleteInfraVuln(vulnInfraID):
    try:
        elastic = getInstance()
        elastic.delete(index=getInfraIndex(), id=str(vulnInfraID))
        return 
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Converts Infra vuln object to Elastic Doc Format

def convertInfraVulnObjectToElasticDoc(vulnObject):
    try:
        processedVuln = {
            "vuln_title": vulnObject.Title,
            "vuln_description": vulnObject.Description,
            "vuln_observation": vulnObject.Observation,
            "vuln_domain": vulnObject.Domain,
            "vuln_subdomain": vulnObject.Subdomain,
            "vuln_extra_info": vulnObject.ExtraInfo,
            "vuln_cvss_score": vulnObject.CVSS_Score,
            "vuln_language": vulnObject.Language,
            "vuln_severity": vulnObject.Severity,
            "vuln_recommendation": vulnObject.Recommendation,
            "vuln_date": vulnObject.Date,
            "vuln_status": vulnObject.Status
        }   
        return processedVuln
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets all Infra vulns from Elastic 

def getAllInfraVulnsFromElastic():
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), query={"match_all":{}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vulns by Date from Elastic

def getAllInfraVulnsByDateFromElastic(fromDate, toDate):
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate), "lte":str(toDate)}}}]}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vulns by Severity 

def getInfraVulnsBySeverity(severity):
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"match":{"vuln_severity":str(severity)}}]}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vulns by Severity and Date

def getInfraVulnsBySeverityDate(severity, fromDate, toDate):
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_severity":str(severity)}}]}}]}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vulns by Status

def getInfraVulnsByStatus(status):
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"match":{"vuln_status":str(status)}}]}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vulns by Status and Date

def getInfraVulnsByStatusDate(status, fromDate, toDate):
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_status":str(status)}}]}}]}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vulns by Severity and Status

def getInfraVulnsBySeverityStatus(severity, status):
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"match":{"vuln_status":str(status)}},
        {"match":{"vuln_severity":str(severity)}}]}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vulns by Severity, Status and Date

def getInfraVulnsBySeverityStatusDate(severity, status, fromDate, toDate):
    try:
        elastic = getInstance()
        InfraVulns = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"bool":{"must":[{"range":{"vuln_date":{"gte":str(fromDate),"lte":str(toDate)}}}]}}, 
        {"bool":{"must":[{"match":{"vuln_status":str(status)}},{"match":{"vuln_severity":str(severity)}}]}}]}}, size=int(getInfraVulnCount()))
        return InfraVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra vuln by ID from Elastic

def getInfraVulnByIDFromElastic(vulnID):
    try:
        elastic = getInstance()
        InfraVuln = elastic.search(index=getInfraIndex(), 
        query={"bool":{"must":[{"match":{"_id":str(vulnID)}}]}})
        return InfraVuln
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass    

#Gets Infra total vulns count

def getInfraVulnCount():
    try:
        elastic = getInstance()
        totalVulns=elastic.search(index=getInfraIndex())
        return totalVulns['hits']['total']['value']
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets Infra index from settings

def getInfraIndex():
    return ELASTIC_SEARCH["ELASTIC_INDEX"]["infra_vulns"]