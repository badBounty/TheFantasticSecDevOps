import json
import csv
import sys
import datetime

sourcePath = sys.argv[1]
outputPath = sys.argv[2]
projName = sys.argv[3]
flawfinderFinalJson = []

def outputFlawfinderResults(flawfinderFinalJson):
    try:
        with open(outputPath,'w') as flawfinderJSON:
            json.dump(flawfinderFinalJson,flawfinderJSON,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def defineVulnSeverity(vuln):
    vulnState = ""
    if vuln == "4":
        vulnState = "High"
    elif vuln == "3":
        vulnState = "Medium"
    elif vuln == "2":
        vulnState = "Low"
    elif vuln == "1":
        vulnState = "Informational"
    return vulnState

def initParser():
    print("------------------------------------------")
    print("New Flawfinder JSON Parsing \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    fileCSV = open(sourcePath, mode='r')
    return fileCSV

def parseCSV():
    try:
        fileCSV = initParser()      
        csv_reader = csv.reader(fileCSV,delimiter=',')
        next(csv_reader)
        for row in csv_reader:
            loadCSV(row)
        outputFlawfinderResults(flawfinderFinalJson)
    except:
        print("Error: ", sys.exc_info()[0])
        pass

def loadCSV(row):  
    try:
        vulnAffectedCode = row[11].lstrip()
        flawfinderJSON = {
            'title': row[5],
            'component': row[0].replace(f"/home/{projName}/",""),
            'severity': defineVulnSeverity(row[4]),
            'affectedCode' : vulnAffectedCode,
            'line' : row[1],
            'description' : row[7]
        }
        flawfinderFinalJson.append(flawfinderJSON)        
    except:
        print("-----------------")
        print("Linea fallida:\n",row)
        print("-----------------\n")
        print("Error: ", sys.exc_info()[0])

parseCSV()
