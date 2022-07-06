import sys
from datetime import datetime
from elasticsearch import Elasticsearch
from VulneryAPI.settings import ELASTIC_SEARCH, LOGGER_APP_NAMES
from VulneryAPI.Backend import logger
from VulnsPoster.models import *

#--------------Generic--------------#

#Gets instance of Elastic

def getInstance(url, port, username, password):
    return Elasticsearch([f'http://{url}:{port}'], http_auth=(username, password), verify_certs=False)

#---------------SAST----------------#

#Uploads SAST vuln into Elastic

def uploadSASTVuln(vulnSAST):
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        processedVulnSAST = convertSASTVulnObjectToElasticDoc(vulnSAST)
        elastic.index(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["sast_vulns"],body=processedVulnSAST)
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
            "vuln_severity": vulnObject.Severity, #Store SeverityID
            "vuln_recommendation": "",
            "vuln_date": vulnObject.Date,
            "vuln_status": vulnObject.Status #Store StatusID
        }   
        return processedVuln  
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets SAST vulns from Elastic 

def getAllSASTVulnsFromElastic():
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        SASTVulns = elastic.search(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["sast_vulns"], query={"match_all":{}})
        return SASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#---------------DAST----------------#

#Uploads DAST vuln into Elastic

def uploadDASTVuln(vulnDAST):
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        processedVulnDAST = convertDASTVulnObjectToElasticDoc(vulnDAST)
        elastic.index(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["dast_vulns"],body=processedVulnDAST)
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
            "vuln_severity": vulnObject.Severity, #Store SeverityID
            "vuln_recommendation": vulnObject.Recommendation,
            "vuln_date": vulnObject.Date,
            "vuln_status": vulnObject.Status #Store StatusID
        }   
        return processedVuln  
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets DAST vulns from Elastic 

def getAllDASTVulnsFromElastic():
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        DASTVulns = elastic.search(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["dast_vulns"], query={"match_all":{}})
        return DASTVulns
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#-------------Severity---------------#   

#Gets all Severities from Elastic

def getAllSeveritiesFromElastic():
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        Severities = elastic.search(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["vuln_severity"], query={"match_all":{}})
        return Severities
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets a severity ID by the severity name from Elastic

def getSeverityIDByNameFromElastic(severityName):
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        severityID = elastic.search(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["vuln_severity"], 
        query={"bool":{"must":[{"match":{"severity":severityName}}]}}) 
        return severityID['hits']['hits'][0]['_id']
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#--------------States---------------#

#Gets all States from Elastic

def getAllStatesFromElastic():
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        States = elastic.search(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["vuln_status"], query={"match_all":{}})
        return States
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

#Gets a status ID by the status name from Elastic

def getStatusIDByNameFromElastic(statusName):
    try:
        elastic = getInstance(ELASTIC_SEARCH["ELASTIC_CONNECTION"]["URL"],ELASTIC_SEARCH["ELASTIC_CONNECTION"]["PORT"],
        ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["username"],ELASTIC_SEARCH["ELASTIC_CREDENTIALS"]["password"])
        statusID = elastic.search(index=ELASTIC_SEARCH["ELASTIC_INDEX"]["vuln_status"], 
        query={"bool":{"must":[{"match":{"status":statusName}}]}}) 
        return statusID['hits']['hits'][0]['_id']
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass