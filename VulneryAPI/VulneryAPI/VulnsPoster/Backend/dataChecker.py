from VulnsPoster.models import *
from VulneryAPI.Backend import logger
from VulneryAPI.settings import ALL, LOGGER_APP_NAMES, VULN_SEVERITY, VULN_STATUS, VULN_TYPES

import json
import datetime 
import sys

#Checks if vuln type is valid for SAST.

def checkVulnTypeSAST(vulnTypeRecieved):
    try:
        trimmedVulnType = vulnTypeRecieved.strip()
        if trimmedVulnType == VULN_TYPES['SAST_Vulns']:
            return True
        return False
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Checks if vuln type is valid for DAST.

def checkVulnTypeDAST(vulnTypeRecieved):
    try:
        trimmedVulnType = vulnTypeRecieved.strip()
        if trimmedVulnType == VULN_TYPES['DAST_Vulns']:
            return True
        return False
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())


#Checks if vuln type is valid for Infra.

def checkVulnTypeInfra(vulnTypeRecieved):
    try:
        trimmedVulnType = vulnTypeRecieved.strip()
        if trimmedVulnType == VULN_TYPES['Infra_Vulns']:
            return True
        return False
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def checkJSONValid(jsonData):
    try:
        jsonDataProcessed = json.loads(jsonData) 
        for key in jsonDataProcessed:
            if key == "":
                return False
            return True    
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        return False

def checkVulnDates(fromDateRecieved, toDateRecieved):
    try:
        if fromDateRecieved and toDateRecieved:
            fromDate = datetime.datetime.strptime(fromDateRecieved, '%Y-%m-%d')
            toDate = datetime.datetime.strptime(toDateRecieved, '%Y-%m-%d')
            if toDate >= fromDate:
                return True
        return False
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def checkVulnSeverityAndStatusForPosting(severity, status):
    try:
        if severity in VULN_SEVERITY.__str__() and status in VULN_STATUS.__str__():
            return True
        return False
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())