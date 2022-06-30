from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.views.decorators.csrf import csrf_exempt

from VulnsPoster.Backend import vulnsPoster 
from orchestrator.Backend import logger

#View del post de vulns a Elasticsearch.

@csrf_exempt
def postVulns(request):
    if request.method == "POST":
        vulnJSON = vulnsPoster.recieveVuln(request)
        #logger.logInfo("vulnsPoster", vulnJSON)
        vulnsPoster.processVuln(vulnJSON)

        #Listar vulns en html

        #Hacer POST de vulns a Elasticsearch.
        return render(request, "vulns.html", {'list':vulnJSON})
    #Traer error que no se puede GET.
    return HttpResponseRedirect('/vulnsPoster/')

#View de las vulns traidas de Elasticsearch.

def vulns(request):
    #Hacer un render con un html.
    return render(request, "vulns.html")

#View de los errores de posteo de vulns. 

def vulnsErrors(request):
    return HttpResponse("Aca se traen las vulns que dieron error en el posteo al server")


