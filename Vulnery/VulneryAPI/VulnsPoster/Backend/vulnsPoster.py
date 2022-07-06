from VulnsPoster.models import *
from VulneryAPI.Backend import logger
from VulneryAPI.Backend import elasticsearch
from VulneryAPI.settings import LOGGER_APP_NAMES

import json
import sys

#--------------Generic--------------#

#Recieves the vuln

def recieveVuln(request):
    try:
        vulnJSON = request.body
        return json.loads(vulnJSON.decode('utf-8'))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Sets the vuln severity and state to an ID

def setVulnAttributeToID(vulnObject):
    try:
        severityID = elasticsearch.getSeverityIDByNameFromElastic(vulnObject.Severity)
        statusID = elasticsearch.getStatusIDByNameFromElastic(vulnObject.Status)
        severityObject = Severity.createSeverity(severityID, vulnObject.Severity)
        statusObject = Status.createStatus(statusID, vulnObject.Status)
        vulnObject.Severity = severityObject.severityID
        vulnObject.Status = statusObject.statusID
        return vulnObject
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------SAST----------------#
        
#Process the SAST vuln

def processSASTVuln(vulnJSON):
    try:
        vulnObject = VulnSAST.createVuln(vulnJSON) 
        return vulnObject
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
   
#Saves the SAST vuln to Elastic

def uploadSASTVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.uploadSASTVuln(setVulnAttributeToID(vulnObject))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all SAST vulns from Elastic

def getAllSASTVulnsFromElastic():
    try:
        return elasticsearch.getAllSASTVulnsFromElastic()
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------DAST----------------#

#Process the SAST vuln

def processDASTVuln(vulnJSON):
    try:
        vulnObject = VulnDAST.createVuln(vulnJSON) 
        return vulnObject
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
   
#Saves the DAST vuln to Elastic

def uploadDASTVulnIntoElastic(vulnObject):
    try:
        return elasticsearch.uploadDASTVuln(setVulnAttributeToID(vulnObject))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Gets all DAST vulns from Elastic

def getAllDASTVulnsFromElastic():
    try:
        return elasticsearch.getAllDASTVulnsFromElastic()
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#-------------Severity---------------#   

#Gets a severity ID by the severity name

def getSeverityIDByNameFromElastic(severityName):
    try:
        return elasticsearch.getSeverityIDByNameFromElastic(severityName)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())