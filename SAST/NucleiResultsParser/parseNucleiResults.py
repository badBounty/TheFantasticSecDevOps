import json
import sys
import datetime

sourcePath = sys.argv[1]
outputPath = sys.argv[2]
projName = sys.argv[3]
nucleiFinalJson = []
nucleiLines = 0

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
    global nucleiLines
    try:
        fileJSON = initParser()      
        for line in fileJSON.readlines():
            if line != "\n":
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
                nucleiLines += 1         
        outputNucleiResults(nucleiFinalJson)
    except:
        print("Error: ", sys.exc_info()[0])
        pass

def loadJSON(line):
    global nucleiLines    
    try:
        issue = json.loads(line)
        nucleiComponent = issue["host"]
        nucleiComponent = nucleiComponent.replace(f"/home/{projName}/","")
        nucleiJson = {
            'title': issue["templateID"],
            'component': nucleiComponent,
            'severity': issue["info"]["severity"],
            'affectedCode' : issue["extracted_results"]
        }
        nucleiFinalJson.append(nucleiJson)        
    except:
        print("-----------------")
        print("Linea fallida:\n",line)
        print("\nNumero de linea: ",nucleiLines+1)
        print("-----------------\n")

parseJSON()
