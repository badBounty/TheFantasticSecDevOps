import sys
import datetime
import csv
import pymongo
from elasticsearch import Elasticsearch

csvFile = sys.argv[1]
mongoURL = sys.argv[2]
mongoPORT = sys.argv[3]
elasticURL = sys.argv[4]
elasticPORT = sys.argv[5]

vulnsJSONError = []

mongoConnection = None
infraVulns = None

risks = {
    "None": "Informational",
}

def openCSVFile():
    initPoster()
    try:
        with open(csvFile, newline='') as csvOpenedFile:
            dictReader = csv.DictReader(csvOpenedFile)
            postVulnToMongoDB(dictReader)
    except:
        printError()
    #printVulnJSONError()
    
def printVulnJSONError():
    if not vulnsJSONError: 
        print("\nNo errors in vulns were found. \n")
        successPoster()
    else:
        print("\nThe following vulns showed an error: \n")
        for item in vulnsJSONError:
            print(f"\n{item}\n")

def postVulnToMongoDB(dictReader):
    try:
        global mongoConnection
        global infraVulns
        mongoConnection = mongoConnect()
        infraVulns = getInfraCollection()
        if mongoConnection:
            for row in dictReader:
                try:
                    vulnJSON = {
                        "domain": row['Host'],
                        "resource": "N/A", #target?
                        "vulnerability_name": row['Name'],
                        "observation": getJSONObservation(row),
                        "extra_info": row['Synopsis'] if row['Synopsis'] else "N/A",
                        "image_string": "N/A",
                        "file_string": "N/A",
                        "date_found": "N/A", #getScanDate
                        "last_seen": "N/A", #getScanDate
                        "language": "N/A",
                        "cvss_score": row['CVSS v2.0 Base Score'],
                        "vuln_type": "Infra",
                        "state": "new"
                    }
                    addInfraVuln(vulnJSON, infraVulns)
                except:
                    printError()     
        else:
            print("\nError trying to connect to MongoDB.\n")     
    except:
        printError()
    
def addInfraVuln(vulnJSON, infraVulns):
    try:
        exists = infraVulns.find_one({'domain': vulnJSON['domain'], 'resource': vulnJSON['resource'], 
        'vulnerability_name': vulnJSON['vulnerability_name'], 'language': vulnJSON['language'], 'observation': vulnJSON['observation']})
        if exists:
            updateVulnMongoDB(infraVulns, vulnJSON, exists)
        else:
            _id = insertVulnMongoDB(infraVulns, vulnJSON)
            if _id is not None:
                print(getReturnSuccessMessageDB(vulnJSON,'MongoDB','inserted')) 
                vulnJSON['_id'] = str(_id.inserted_id)
                insertVulnElasticDB(vulnJSON)
            else:
                print(getReturnFailedMessageDB(vulnJSON, 'MongoDB', 'inserted'))  
    except:
        printError()
    
def updateVulnMongoDB(infraVulns, vulnJSON, exists):
    try:
        infraVulns.update_one({'_id': exists.get('_id')}, {'$set': {
            'extra_info': vulnJSON['Synopsis'] if vulnJSON['Synopsis'] else "N/A",
            'last_seen': "N/A", #getScanDate from VulnJSON
            'image_string': "N/A",
            'file_string': "N/A",
            'state': 'new' if exists['state'] != 'rejected' else exists['state']
        }})
        print(getReturnSuccessMessageDB(vulnJSON,'MongoDB','updated')) 
    except:
        printError()
        print(getReturnFailedMessageDB(vulnJSON, 'MongoDB', 'updated'))
        appendJSONError(vulnJSON)

def insertVulnMongoDB(infraVulns, vulnJSON):
    try:
        return infraVulns.insert_one(vulnJSON)
    except:
        printError()       
        appendJSONError(vulnJSON)

def appendJSONError(vulnJSON):
    vulnsJSONError.append(f"Error: {sys.exc_info()}. Vuln: ")
    vulnsJSONError.append(vulnJSON)

def getReturnSuccessMessageDB(vulnJSON, database, action):
    return f"\nThe vuln '{vulnJSON['vulnerability_name']}' was SUCCESSFULLY {action} into {database}.\n"

def getReturnFailedMessageDB(vulnJSON, database, action):
    f"\nThe vuln '{vulnJSON['vulnerability_name']}' COULD NOT BE {action} into {database}.\n" 

def insertVulnElasticDB(vulnJSON):
    try:
        elasticConnection = elasticsearchConnect()
        if elasticConnection:
            vulnJSONElastic = {
                'vulnerability_id': str(vulnJSON['_id']),
                'vulnerability_domain': vulnJSON['domain'],
                'vulnerability_subdomain': vulnJSON['resource'],
                'vulnerability_vulnerability_name': vulnJSON['vulnerability_name'],
                'vulnerability_observation': vulnJSON['observation'],
                'vulnerability_extra_info': vulnJSON['extra_info'],
                'vulnerability_date_found': vulnJSON['date_found'],
                'vulnerability_last_seen': vulnJSON['last_seen'],
                'vulnerability_language': vulnJSON['language'],
                'vulnerability_cvss_score': vulnJSON['cvss_score'],
                'vulnerability_cvss3_severity': resolveSeverity(vulnJSON['cvss_score']),
                'vulnerability_vuln_type': vulnJSON['vuln_type'],
                'vulnerability_state': vulnJSON['state']
            }
            elasticConnection.index(index='infra_vulnerabilities',doc_type='_doc',id=vulnJSONElastic['vulnerability_id'],body=vulnJSONElastic)
            print(getReturnSuccessMessageDB(vulnJSON,'Elasticsearch')) 
        else:
            printError()
            print("\nError trying to connect to Elasticsearch.\n")
    except:
        printError()
        print(getReturnFailedMessageDB(vulnJSON, 'Elasticsearch', 'inserted or updated'))

def updateElasticDB(): #TODO
    global mongoConnection
    global infraVulns
    mongoConnection = mongoConnect()
    infraVulns = getInfraCollection()
    try:
        retrievedInfraVulns = infraVulns.find()
        for vuln in retrievedInfraVulns:
            insertVulnElasticDB(vuln)
    except:
        printError()

def mongoConnect():
    return pymongo.MongoClient(f"mongodb://{mongoURL}:{mongoPORT}/",connect=False)

def getInfraCollection():
    mongoDB = "Project"
    return mongoConnection[mongoDB]['infra_vulnerabilities']

def elasticsearchConnect():
    return Elasticsearch(f'http://{elasticURL}:{elasticPORT}')

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

def getScanDate(row):
    pluginOutput = row['Plugin Output']
    #Ver de convertir a json y obtener el Scan Start Date.

def initPoster():
    print("------------------------------------------")
    print("New Infra Vulns --> MongoDB Posting \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")

def successPoster():
    print("Post to MongoDB and Elasticsearch has succeeded. You may check the pertinent dashboard on Kibana\n")

def printError():
    print("---------------------------\n")
    print("Error:\n")
    print(sys.exc_info())
    
openCSVFile()
