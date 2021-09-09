import json
import sys
import os.path
import datetime

nucleiTitle = "" #templateID
nucleiComponent = "" #host
nucleiSeverity = "" #info->severity
nucleiAffectedCode = [] #extracted-results
sourcePath = sys.argv[1]
outputPath = sys.argv[2]
nucleiFinalJson = []

def jsonLoad():
    try:
        with open(sourcePath) as nucleiTxt:
            nucleiResults = json.load(nucleiTxt)
        return nucleiResults
    except:
        print("Error al cargar el resultado de nuclei: ",sys.exc_info()[0])
        raise

def nucleiJsonParse():
    print("------------------------------------------")
    print("New Nuclei JSON Parsing \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    nucleiResults = jsonLoad()
    if(nucleiResults is not None):
        try:
            for issue in nucleiResults:
                nucleiTitle = issue["templateID"]
                nucleiComponent = issue["host"]
                nucleiSeverity = issue["info"]["severity"]
                nucleiAffectedCode = issue["extracted_results"]
                nucleiJson = {
                    'title': issue["templateID"],
                    'component': issue["host"],
                    'severity': issue["info"]["severity"],
                    'affectedCode' : issue["extracted_results"]
                }
                nucleiFinalJson.append(nucleiJson)
            outputNucleiResults(nucleiFinalJson)
        except:
            print("Error: no se pudo parsear.",sys.exc_info()[0])
            pass
    else:
        sys.exit(5)
   
def outputNucleiResults(nucleiFinalJson):
    try:
        with open(outputPath,'w') as nucleiJSON:
            json.dump(nucleiFinalJson,nucleiJSON,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

nucleiJsonParse()
