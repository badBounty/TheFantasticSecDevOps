from django.shortcuts import render, redirect
from django.http import HttpResponse, HttpResponsePermanentRedirect, HttpResponseRedirect, JsonResponse
from django.views.decorators.csrf import csrf_exempt

import sys

from VulnsPoster.Backend import vulnsPoster, dataChecker
from VulneryAPI.Backend import logger
from VulneryAPI.Backend.Security import encryption, login as loginProcess
from VulneryAPI.settings import LOGGER_APP_NAMES

#View default page.

def mainPage(request):
    return render(request, "mainPage.html")

#View Login.

def login(request):
    #Check if user is logged in asking for cookie. If so, don't let him go to login.
    #If user is not logged in or cookie is invalid, continue to login.
    return render(request, "login.html")

#View validateLogin

@csrf_exempt
def validateLogin(request):
    if request.method == "POST":
        try:
            #Check valid data.
            #Grab username and encrypt. Check if user exists.
            #If exists, hash the password and compare with the stored.
            #If success, generate cookie, store it in database and return to client.
            return HttpResponsePermanentRedirect('/home/')
        except:
            logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
    else:
        return HttpResponseRedirect('login/')
