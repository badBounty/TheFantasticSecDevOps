from django.conf import settings
from elasticsearch import Elasticsearch
from VulnsPoster import models
from VulneryAPI.Backend import logger
from VulnsPoster.Backend import elasticsearch
from VulneryAPI import settings

import json
import requests
import sys


#Recieves the SAST vuln

def recieveSASTVuln(request):
    try:
        vulnJSON = request.body
        return json.loads(vulnJSON.decode('utf-8'))
    except:
        logErrors()
        
#Process the SAST vuln

def processSASTVuln(vulnJSON):
    try:
        newVuln = models.Vuln.createVuln(vulnJSON)
        

        logger.setLoggerLevel("INFO")
        logger.logInfo("vulnsPoster", newVuln)
    except:
        logErrors()

#Saves the SAST vuln to Elastic

def uploadSASTVulnIntoElastic(vulnObject):
    try:
        elastic = elasticsearch.getInstance(settings["ELASTIC_CONNECTION"]["URL"],settings["ELASTIC_CONNECTION"]["PORT"],
        settings["ELASTIC_CREDENTIALS"]["username"],settings["ELASTIC_CREDENTIALS"]["password"]) #TODO
        elasticsearch.uploadSASTVuln(elastic, vulnObject)
        #Enviar vuln SAST a elastic y ver que responde.
    except:
        logErrors()

#Logs errors

def logErrors():
    logger.setLoggerLevel("ERROR")
    logger.logError("vulnsPoster",sys.exc_info())