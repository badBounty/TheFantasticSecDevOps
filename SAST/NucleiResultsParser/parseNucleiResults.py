import json
import sys
import datetime

nucleiTitle = "" #templateID
nucleiComponent = "" #host
nucleiSeverity = "" #info->severity
nucleiAffectedCode = [] #extracted-results
sourcePath = sys.argv[1]
outputPath = sys.argv[2]
nucleiFinalJson = []

def outputNucleiResults(nucleiFinalJson):
    try:
        with open(outputPath,'w') as nucleiJSON:
            json.dump(nucleiFinalJson,nucleiJSON,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def initParser():
    print("------------------------------------------")
    print("New Nuclei JSON Parsing \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    fileJSON = open(sourcePath,'r')
    return fileJSON

def parseJSON():
    try:
        fileJSON = initParser()      
        for line in fileJSON.readlines():
            if "}{" in line:
                brokenJSON = line.split("}{")
                brokenJSON[0] += "}"
                brokenJSON[1] = "{" + brokenJSON[1]
                if 'Z"}' not in brokenJSON[1]:
                    brokenJSON[1] += "}"
                loadJSON(brokenJSON[0])
                loadJSON(brokenJSON[1])           
            else:
                loadJSON(line)
        outputNucleiResults(nucleiFinalJson)
    except:
        print("Error: "+ sys.exc_info()[0])
        pass

def loadJSON(line):
    try:
        issue = json.loads(line)
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
    except:
        print("Linea fallida:\n" + line)

parseJSON()
