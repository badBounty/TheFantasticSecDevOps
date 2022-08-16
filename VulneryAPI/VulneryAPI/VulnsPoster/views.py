from datetime import datetime
import json
from django.shortcuts import render
from django.http import HttpResponse, HttpResponsePermanentRedirect, HttpResponseRedirect, JsonResponse
from django.views.decorators.csrf import csrf_exempt

import sys

from VulnsPoster.Backend import vulnsPoster, dataChecker
from VulneryAPI.Backend import logger
from VulneryAPI.settings import ALL, LOGGER_APP_NAMES

#---------------Vulns----------------#

#Vulns view. Renders the vulns.html template.

def vulns(request):
    try:
        if request.method == "GET":
            return render(request, "vulns.html")
        else:
            pass #Print error or forbidden. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------SAST----------------#

#SAST POST view. POST to DB.

@csrf_exempt
def postVulnSAST(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            vulnJSON = vulnsPoster.recieveVuln(request)
            vulnObject = vulnsPoster.processSASTVuln(vulnJSON)
            vulnsPoster.uploadSASTVulnIntoElastic(vulnObject)
            return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#Update SAST vuln view.

@csrf_exempt
def updateVulnSAST(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            if dataChecker.checkJSONValid(request.body):
                vulnJSON = vulnsPoster.recieveVuln(request)
                if dataChecker.checkVulnSeverityAndStatusForPosting(vulnJSON['Severity'], vulnJSON['Status']):
                    vulnObject = vulnsPoster.processSASTVuln(vulnJSON)
                    vulnObject.vulnID = str(vulnJSON['vulnID'])
                    if 'Date' in vulnJSON:
                        vulnObject.Date = datetime.strptime(vulnJSON['Date'],"%Y-%m-%d").date()
                    vulnsPoster.updateSASTVulnIntoElastic(vulnObject)
                    return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#Delete SAST vuln view.

@csrf_exempt
def deleteVulnSAST(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            vulnID = json.loads(request.body)
            vulnsPoster.deleteSASTVulnFromElastic(vulnID['vulnID'])
            return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#SAST vulns view. Returns SAST vulns by parameters or a single SAST vuln.

@csrf_exempt #Check if request is able to get vulns. TODO
def vulnsSAST(request):
    try:
        if request.method == "POST":        
            return vulnsSASTMethodPost(request)
        elif request.method == "GET":
            vulnID = request.GET.get('vulnID','')
            return HttpResponse(vulnsPoster.getSASTVulnByIDFromElastic(vulnID))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def vulnsSASTMethodPost(request):
    try: 
        if dataChecker.checkJSONValid(request.body): 
            jsonData = json.loads(request.body)
            vulnType = jsonData['vulnType']
            severity = jsonData['severity']
            status = jsonData['status']
            fromDate = jsonData['fromDate']
            toDate = jsonData['toDate']
            getAllDates = jsonData['getAllDates']
            if dataChecker.checkVulnTypeSAST(vulnType): 
                if getAllDates == True: #Fix if dates are empty and it's not checked. TODO   
                    return vulnsSASTAllDates(severity, status)
                else:
                    if dataChecker.checkVulnDates(fromDate, toDate):
                        return vulnsSASTDates(severity, status, fromDate, toDate)
                    else:
                        pass #Return toDate is greater than fromDate. TODO
            else:
                pass #Return type is not SAST. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

def vulnsSASTAllDates(severity, status):
    try:
        if severity == ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllSASTVulnsFromElastic())
        elif severity != ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllSASTVulnsBySeverityFromElastic(severity))
        elif severity == ALL and status != ALL:
            return HttpResponse(vulnsPoster.getAllSASTVulnsByStatusFromElastic(status))
        else:
            return HttpResponse(vulnsPoster.getAllSASTVulnsBySeverityStatusFromElastic(severity, status))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def vulnsSASTDates(severity, status, fromDate, toDate):
    try:
        if severity == ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllSASTVulnsByDateFromElastic(fromDate, toDate))
        elif severity != ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllSASTVulnsBySeverityDateFromElastic(severity, fromDate, toDate))
        elif severity == ALL and status != ALL:
            return HttpResponse(vulnsPoster.getAllSASTVulnsByStatusDateFromElastic(status, fromDate, toDate))
        else:
            return HttpResponse(vulnsPoster.getAllSASTVulnsBySeverityStatusDateFromElastic(severity, status, fromDate, toDate))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------DAST----------------#        

#DAST POST view. POST to DB.

@csrf_exempt
def postVulnDAST(request):
    #Check if request is able to do POST TODO
    if request.method == "POST":
        try:
            vulnJSON = vulnsPoster.recieveVuln(request)
            vulnObject = vulnsPoster.processDASTVuln(vulnJSON)
            vulnsPoster.uploadDASTVulnIntoElastic(vulnObject)
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],vulnJSON)
            return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/vulnsPoster/')

#Update DAST vuln view.

@csrf_exempt
def updateVulnDAST(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            if dataChecker.checkJSONValid(request.body):
                vulnJSON = vulnsPoster.recieveVuln(request)
                if dataChecker.checkVulnSeverityAndStatusForPosting(vulnJSON['Severity'], vulnJSON['Status']):
                    vulnObject = vulnsPoster.processDASTVuln(vulnJSON)
                    vulnObject.vulnID = str(vulnJSON['vulnID'])
                    if 'Date' in vulnJSON:
                        vulnObject.Date = datetime.strptime(vulnJSON['Date'],"%Y-%m-%d").date()
                    vulnsPoster.updateDASTVulnIntoElastic(vulnObject)
                    return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#Delete DAST vuln view.

@csrf_exempt
def deleteVulnDAST(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            vulnID = json.loads(request.body)
            vulnsPoster.deleteDASTVulnFromElastic(vulnID['vulnID'])
            return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#DAST vulns view. Returns all the DAST vulns.

@csrf_exempt #Check if request is able to get vulns. TODO
def vulnsDAST(request):
    try:
        if request.method == "POST":   
            vulns = vulnsDASTMethodPost(request)
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],vulns.content)
            return vulns
        elif request.method == "GET":
            vulnID = request.GET.get('vulnID','')
            return HttpResponse(vulnsPoster.getDASTVulnByIDFromElastic(vulnID))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def vulnsDASTMethodPost(request):
    try: 
        if dataChecker.checkJSONValid(request.body): 
            jsonData = json.loads(request.body)
            vulnType = jsonData['vulnType']
            severity = jsonData['severity']
            status = jsonData['status']
            fromDate = jsonData['fromDate']
            toDate = jsonData['toDate']
            getAllDates = jsonData['getAllDates']
            if dataChecker.checkVulnTypeDAST(vulnType): 
                if getAllDates == True: #Fix if dates are empty and it's not checked. TODO   
                    return vulnsDASTAllDates(severity, status)
                else:
                    if dataChecker.checkVulnDates(fromDate, toDate):
                        return vulnsDASTDates(severity, status, fromDate, toDate)
                    else:
                        pass #Return toDate is greater than fromDate. TODO
            else:
                pass #Return type is not SAST. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

def vulnsDASTAllDates(severity, status):
    try:
        if severity == ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllDASTVulnsFromElastic())
        elif severity != ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllDASTVulnsBySeverityFromElastic(severity))
        elif severity == ALL and status != ALL:
            return HttpResponse(vulnsPoster.getAllDASTVulnsByStatusFromElastic(status))
        else:
            return HttpResponse(vulnsPoster.getAllDASTVulnsBySeverityStatusFromElastic(severity, status))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def vulnsDASTDates(severity, status, fromDate, toDate):
    try:
        if severity == ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllDASTVulnsByDateFromElastic(fromDate, toDate))
        elif severity != ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllDASTVulnsBySeverityDateFromElastic(severity, fromDate, toDate))
        elif severity == ALL and status != ALL:
            return HttpResponse(vulnsPoster.getAllDASTVulnsByStatusDateFromElastic(status, fromDate, toDate))
        else:
            return HttpResponse(vulnsPoster.getAllDASTVulnsBySeverityStatusDateFromElastic(severity, status, fromDate, toDate))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#---------------Infra----------------#

#Infra POST view. POST to DB.

@csrf_exempt
def postVulnInfra(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            vulnJSON = vulnsPoster.recieveVuln(request)
            vulnObject = vulnsPoster.processInfraVuln(vulnJSON)
            vulnsPoster.uploadInfraVulnIntoElastic(vulnObject)
            return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#Update Infra vuln view.

@csrf_exempt
def updateVulnInfra(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            if dataChecker.checkJSONValid(request.body):
                vulnJSON = vulnsPoster.recieveVuln(request)
                if dataChecker.checkVulnSeverityAndStatusForPosting(vulnJSON['Severity'], vulnJSON['Status']):
                    vulnObject = vulnsPoster.processInfraVuln(vulnJSON)
                    vulnObject.vulnID = str(vulnJSON['vulnID'])
                    if 'Date' in vulnJSON:
                        vulnObject.Date = datetime.strptime(vulnJSON['Date'],"%Y-%m-%d").date()
                    vulnsPoster.updateInfraVulnIntoElastic(vulnObject)
                    return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#Delete Infra vuln view.

@csrf_exempt
def deleteVulnInfra(request):
    #Check if request is able to do POST. TODO
    if request.method == "POST":
        try:
            vulnID = json.loads(request.body)
            vulnsPoster.deleteInfraVulnFromElastic(vulnID['vulnID'])
            return HttpResponse("OK")
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass 
    else:
        return HttpResponseRedirect('/')

#Infra vulns view. Returns Infra vulns by parameters or a single Infra vuln.

@csrf_exempt #Check if request is able to get vulns. TODO
def vulnsInfra(request):
    try:
        if request.method == "POST":        
            return vulnsInfraMethodPost(request)
        elif request.method == "GET":
            vulnID = request.GET.get('vulnID','')
            return HttpResponse(vulnsPoster.getInfraVulnByIDFromElastic(vulnID))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def vulnsInfraMethodPost(request):
    try: 
        if dataChecker.checkJSONValid(request.body): 
            jsonData = json.loads(request.body)
            vulnType = jsonData['vulnType']
            severity = jsonData['severity']
            status = jsonData['status']
            fromDate = jsonData['fromDate']
            toDate = jsonData['toDate']
            getAllDates = jsonData['getAllDates']
            if dataChecker.checkVulnTypeInfra(vulnType): 
                if getAllDates == True: #Fix if dates are empty and it's not checked. TODO   
                    return vulnsInfraAllDates(severity, status)
                else:
                    if dataChecker.checkVulnDates(fromDate, toDate):
                        return vulnsInfraDates(severity, status, fromDate, toDate)
                    else:
                        pass #Return toDate is greater than fromDate. TODO
            else:
                pass #Return type is not Infra. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
        pass

def vulnsInfraAllDates(severity, status):
    try:
        if severity == ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllInfraVulnsFromElastic())
        elif severity != ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllInfraVulnsBySeverityFromElastic(severity))
        elif severity == ALL and status != ALL:
            return HttpResponse(vulnsPoster.getAllInfraVulnsByStatusFromElastic(status))
        else:
            return HttpResponse(vulnsPoster.getAllInfraVulnsBySeverityStatusFromElastic(severity, status))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def vulnsInfraDates(severity, status, fromDate, toDate):
    try:
        if severity == ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllInfraVulnsByDateFromElastic(fromDate, toDate))
        elif severity != ALL and status == ALL:
            return HttpResponse(vulnsPoster.getAllInfraVulnsBySeverityDateFromElastic(severity, fromDate, toDate))
        elif severity == ALL and status != ALL:
            return HttpResponse(vulnsPoster.getAllInfraVulnsByStatusDateFromElastic(status, fromDate, toDate))
        else:
            return HttpResponse(vulnsPoster.getAllInfraVulnsBySeverityStatusDateFromElastic(severity, status, fromDate, toDate))
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#-------------VulnType----------------# 

#VulnType view. Returns all the vulns type.

def vulnsType(request):
    try:
        #Check if request is able to get types. TODO
        return HttpResponse(vulnsPoster.getVulnsTypes())
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#-------------Severity----------------# 

#Severity view. Returns all the severities.

def severities(request):
    try:
        return HttpResponse(vulnsPoster.getAllSeverities())
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#--------------Status----------------# 

#Status view. Returns all the status.

def status(request):
    try:
        return HttpResponse(vulnsPoster.getAllStatus())
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())