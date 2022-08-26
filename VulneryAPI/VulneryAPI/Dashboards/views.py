from datetime import datetime
import json
from django.shortcuts import render
from django.http import HttpResponse, HttpResponsePermanentRedirect, HttpResponseRedirect, JsonResponse
from django.views.decorators.csrf import csrf_exempt

import sys

from VulneryAPI.Backend import logger
from VulneryAPI.settings import ALL, LOGGER_APP_NAMES

#---------------Dashboard----------------#

#Vulns view. Renders the dashboard.html template.

def dashboard(request):
    try:
        if request.method == "GET":
            return render(request, "dashboard.html")
        else:
            pass #Print error or forbidden. TODO
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())