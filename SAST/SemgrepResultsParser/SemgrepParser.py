import json
import sys
import datetime

sourcePath = sys.argv[1]
outputPath = sys.argv[2]
projName = sys.argv[3]
semgrepFinalJSON = []

def outputSemgrepResults(semgremFinalJson):
    try:
        with open(outputPath,'w') as semgrepJSON:
            json.dump(semgremFinalJson,semgrepJSON,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def initParser():
    print("------------------------------------------")
    print("New Semgrep JSON Parsing \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    fileJSON = open(sourcePath,'r')
    return fileJSON

def parseJSON():
    try:
        fileJSON = initParser()      
        semgrepJSON = json.load(fileJSON)    
        outputSemgrepResults(loadJSON(semgrepJSON))
    except:
        print("Error: ", sys.exc_info()[0])
        pass

def loadJSON(semgrepJSON):   
    try:
        vulns = semgrepJSON["results"]
        for vuln in vulns:
            semgrepComponent = vuln["path"]
            semgrepComponent = semgrepComponent.replace(f"/home/{projName}/","")
            semgrepFormatJson = {
                'title': vuln["extra"]["message"],
                'component': semgrepComponent,
                'severity': vuln["extra"]["severity"],
                'affectedCode' : vuln["extra"]["lines"],
                'line' : vuln["start"]["line"]  
            }
            print(semgrepFormatJson)
            semgrepFinalJSON.append(semgrepFormatJson)      
        return semgrepFinalJSON 
    except:
        print("-----------------")
        print("Error:\n")
        print("-----------------\n")
        print(sys.exc_info()[0])

parseJSON()
