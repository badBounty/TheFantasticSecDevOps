from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.views.decorators.csrf import csrf_exempt

import sys

from VulnsPoster.Backend import vulnsPoster 
from VulneryAPI.Backend import logger
from VulneryAPI.settings import LOGGER_APP_NAMES

#SAST view. POST to Elastic.

@csrf_exempt
def postSASTVulns(request):
    if request.method == "POST":
        try:
            vulnJSON = vulnsPoster.recieveVuln(request)
            vulnObject = vulnsPoster.processSASTVuln(vulnJSON)
            vulnsPoster.uploadSASTVulnIntoElastic(vulnObject)
            return HttpResponseRedirect('/vulnsPoster/')
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass #Add error vuln to Elastic
    else:
        return HttpResponseRedirect('/vulnsPoster/')

#DAST view. POST to Elastic.

@csrf_exempt
def postDASTVulns(request):
    if request.method == "POST":
        try:
            vulnJSON = vulnsPoster.recieveVuln(request)
            vulnObject = vulnsPoster.processDASTVuln(vulnJSON)
            vulnsPoster.uploadDASTVulnIntoElastic(vulnObject)
            return HttpResponseRedirect('/vulnsPoster/')
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
            pass #Add error vuln to Elastic
    else:
        return HttpResponseRedirect('/vulnsPoster/')

#Vulns view. Gets all the vulns from Elasticsearch.

def vulns(request):
    try:
        SASTVulns = vulnsPoster.getAllSASTVulnsFromElastic()
        DASTVulns = vulnsPoster.getAllDASTVulnsFromElastic()
        sev = vulnsPoster.getSeverityIDByNameFromElastic('low')
        #Load datatable with all vulns and filter by type.
        #Render HTML --> render(request, "vulns.html")
        return HttpResponse(SASTVulns['hits']['hits'])
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Vulns Error view. Gets all the error vulns from Elasticsearch.

def vulnsErrors(request):
    return HttpResponse("Aca se traen las vulns que dieron error en el posteo al server")


