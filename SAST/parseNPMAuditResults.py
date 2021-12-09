import json
import sys
import datetime

sourcePath = sys.argv[1]
outputPath = sys.argv[2]
npmAuditFinalJSON = []

def outputNPMAuditResults(npmAuditFinalJson):
    try:
        with open(outputPath,'w') as npmAuditJSON:
            json.dump(npmAuditFinalJson,npmAuditJSON,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def initParser():
    print("------------------------------------------")
    print("New NPM Audit JSON Parsing \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    fileJSON = open(sourcePath,'r')
    return fileJSON

def parseJSON():
    try:
        fileJSON = initParser()      
        npmAuditJSON = json.load(fileJSON)    
        outputNPMAuditResults(loadJSON(npmAuditJSON))
    except:
        print("Error: ", sys.exc_info())
        pass

def loadJSON(npmAuditJSON):   
    try:
        vulns = npmAuditJSON["vulnerabilities"]
        for vuln in vulns:
            via = vulns[vuln]["via"]
            for item in via:
                if 'title' in item:
                    npmAuditFormatJSON = {  
                        'title': item["title"],
                        'component': vulns[vuln]["nodes"],
                        'severity' : item["severity"],
                        'affectedCode': item["name"]+" - Range: "+item["range"]
                    } 
                    print(npmAuditFormatJSON)
                    npmAuditFinalJSON.append(npmAuditFormatJSON)
        return npmAuditFinalJSON
    except:
        print("-----------------")
        print("Error:\n")
        print("-----------------\n")
        print(sys.exc_info())

parseJSON()
