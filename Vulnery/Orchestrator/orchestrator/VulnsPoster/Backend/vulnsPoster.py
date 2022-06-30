from VulnsPoster import models
from orchestrator.Backend import logger

import json
import requests

def recieveVuln(request):
    vulnJSON = request.body
    return json.loads(vulnJSON.decode('utf-8'))

def processVuln(vulnJSON):
    newVuln = models.Vuln.createVuln(vulnJSON)
    logger.logInfo("vulnsPoster", newVuln)
    pass

def saveVulnIntoElastic(vulnObject):
    #upload to elastic
    pass