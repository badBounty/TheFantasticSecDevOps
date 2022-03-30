from curses.ascii import isdigit
import json
import sys 
import datetime
import csv
import pymongo
import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
from elasticsearch import Elasticsearch
from time import sleep

csvFile = sys.argv[1]
mongoURL = sys.argv[2]
mongoPORT = sys.argv[3]
elasticURL = sys.argv[4]
elasticPORT = sys.argv[5]
outputPath = sys.argv[6]

vulnsJSONError = []

mongoConnection = None
infraVulns = None
counter = 0

risks = {
    "None": "Informational",
}

def openCSVFile():
    global vulnsJSONError
    initPoster()
    try:
        with open(csvFile, newline='') as csvOpenedFile:
            dictReader = csv.DictReader(csvOpenedFile)
            postVulnToMongoDB(dictReader)
    except:
        printError()
    printVulnJSONError(vulnsJSONError)
    
def printVulnJSONError(vulnsJSONError):
    if not vulnsJSONError: 
        print("\nNo errors in vulns were found. \n")
        successPoster()
    else:
        print("\nThere are vulns with errors. \n") #Escribir vulns fallidas a un JSON conteniendo error y json fallido.
        outputVulnErrors(vulnsJSONError)

def postVulnToMongoDB(dictReader):
    try:
        global mongoConnection
        global infraVulns
        mongoConnection = mongoConnect()
        elasticConnection = elasticsearchConnect()
        infraVulns = getInfraCollection()
        if mongoConnection:
            try:
                print(f"\nAdding vulns to MongoDB and Elasticsearch...\n")
                global counter
                for row in dictReader:
                    vulnJSON = None
                    vulnJSON = {
                        "domain": row['Host'],
                        "resource": row['Host'], #target?
                        "vulnerability_name": row['Name'],
                        "observation": getJSONObservation(row),
                        "extra_info": row['Synopsis'] if row['Synopsis'] is not None else "N/A",
                        "image_string": "N/A",
                        "file_string": "N/A",
                        "date_found": datetime.datetime.now().strftime("%Y-%m-%d"'T'"%H:%M:%S"), #getScanDate
                        "last_seen": datetime.datetime.now().strftime("%Y-%m-%d"'T'"%H:%M:%S"), #getScanDate
                        "language": "N/A",
                        "cvss_score": row['CVSS v2.0 Base Score'],
                        "vuln_type": "Infra",
                        "state": "new"
                    }
                    counter+=1
                    print(f"Vuln {counter} : '{row['Name']}' is being inserted in MongoDB and Elasticsearch\n")    
                    addInfraVuln(vulnJSON, infraVulns, counter, elasticConnection)
                    sleep(0.05)
            except:
                printError()     
        else:
            print("\nError trying to connect to MongoDB.\n")     
    except:
        printError()
    
def addInfraVuln(vulnJSON, infraVulns, counter, elasticConnection):
    try:
        exists = infraVulns.find_one({'domain': vulnJSON['domain'], 'resource': vulnJSON['resource'], 
        'vulnerability_name': vulnJSON['vulnerability_name'], 'language': vulnJSON['language'], 'observation': vulnJSON['observation']})
        if exists:
            updateVulnMongoDB(infraVulns, vulnJSON, exists)
            updateElasticDB(elasticConnection)
        else:
            vulnID = insertVulnMongoDB(infraVulns, vulnJSON) 
            vulnJSON['_id'] = vulnID.inserted_id
            insertVulnElasticDB(vulnJSON, elasticConnection)
    except:     
        pass
    
def updateVulnMongoDB(infraVulns, vulnJSON, exists):
    try:
        infraVulns.update_one({'_id': exists.get('_id')}, {'$set': {
            'extra_info': vulnJSON['extra_info'] if vulnJSON['extra_info'] else "N/A", #Fix Synopsis
            'last_seen': datetime.datetime.now().strftime("%Y-%m-%d"'T'"%H:%M:%S"), #getScanDate from VulnJSON
            'image_string': "N/A",
            'file_string': "N/A",
            'state': 'new' if exists['state'] != 'rejected' else exists['state']
        }})
    except:
        appendJSONError(vulnJSON, 'Update', 'MongoDB')
        pass

def insertVulnMongoDB(infraVulns, vulnJSON):
    try:
        return infraVulns.insert_one(vulnJSON)
    except:
        appendJSONError(vulnJSON, 'Insertion', 'MongoDB')
        pass

def appendJSONError(vulnJSON, cause, database):
    vulnsJSONFinalError = {
        "VulnNumber": counter,
        "Database": database,
        "VulnCause": cause,
        "VulnJSON": vulnJSON,
        "VulnError" : str(sys.exc_info())
    }
    vulnsJSONError.append(vulnsJSONFinalError)

def outputVulnErrors(vulnsJSONError):
    try:
        with open(outputPath,'w') as errorsJSON:
            json.dump(vulnsJSONError,errorsJSON,ensure_ascii=False)
        print("\nVuln errors have been written successfuly.\n")
    except:
        print(f"Error: Vuln errors couldn't be written. \n {sys.exc_info()}")
        pass

def insertVulnElasticDB(vulnJSON, elasticConnection):
    try:
        if elasticConnection:
            vulnJSONElastic = {
                'vulnerability_id': str(vulnJSON['_id']), 
                'vulnerability_domain': str(vulnJSON['domain']),
                'vulnerability_subdomain': str(vulnJSON['resource']),
                'vulnerability_vulnerability_name': str(vulnJSON['vulnerability_name']),
                'vulnerability_observation': vulnJSON['observation'],
                'vulnerability_extra_info': str(vulnJSON['extra_info']),
                'vulnerability_date_found': vulnJSON['date_found'],
                'vulnerability_last_seen': vulnJSON['last_seen'],
                'vulnerability_language': str(vulnJSON['language']),
                'vulnerability_cvss_score': str(vulnJSON['cvss_score']),
                'vulnerability_cvss3_severity': str(resolveSeverity(vulnJSON['cvss_score'])),
                'vulnerability_vuln_type': str(vulnJSON['vuln_type']),
                'vulnerability_state': str(vulnJSON['state'])
            }
            try:
                elasticConnection.index(index='infra_vulnerabilities',doc_type='_doc',id=vulnJSONElastic['vulnerability_id'],body=vulnJSONElastic)
            except:
                appendJSONError(vulnJSONElastic, 'Insertion', 'Elasticsearch')
                pass
        else:
            print("\nError trying to connect to Elasticsearch.\n")
    except:
        pass

def updateElasticDB(elasticConnection):
    global infraVulns
    infraVulns = getInfraCollection()
    try:
        retrievedInfraVulns = infraVulns.find()
        for vuln in retrievedInfraVulns:
            insertVulnElasticDB(vuln, elasticConnection)
    except:
        pass

def mongoConnect():
    return pymongo.MongoClient(f"mongodb://{mongoURL}:{mongoPORT}/",connect=False)

def getInfraCollection():
    mongoDB = "Project"
    return mongoConnection[mongoDB]['infra_vulnerabilities']

def elasticsearchConnect():
    return Elasticsearch(f"https://{elasticURL}:{elasticPORT}",http_auth=("elastic","elastic"),ca_certs=False,verify_certs=False)

def getJSONObservation(vuln):
    observation = {
        'title': vuln['Name'],
        'observation_title': vuln['Name'],
        'observation_note': vuln['Synopsis'],
        'implication': vuln['Description'],
        'recommendation_title': vuln['Solution'],
        'recommendation_note': 'N/A',
        'severity': convertSeverity(vuln['Risk'])
    }
    return observation

def convertSeverity(recievedRisk):
    if recievedRisk in risks:
        recievedRisk = risks[recievedRisk]
    return recievedRisk

def resolveSeverity(cvss_score):
    try:
        if cvss_score:
            if isdigit(cvss_score):
                cvss_score = float(cvss_score)
                if cvss_score == 0:
                    return 'Informational'
                elif 0 < cvss_score <= 3.9:
                    return 'Low'
                elif 3.9 < cvss_score <= 6.9:
                    return 'Medium'
                elif 6.9 < cvss_score <= 8.9:
                    return 'High'
                else:
                    return 'Critical'
            else:
                return cvss_score
        else:
            return 'None'
    except:
        pass

def getScanDate(row):
    pluginOutput = row['Plugin Output']
    #Ver de convertir a json y obtener el Scan Start Date.

def initPoster():
    print("------------------------------------------\n")
    print("New MongoDB and Elasticsearch Infra Vulns Posting \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y")+"\n")

def successPoster():
    print("Process finished.\n")

def printError():
    print("------------------------------------------\n")
    print("Error:\n")
    print(sys.exc_info())
    
openCSVFile()
