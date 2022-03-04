import sys
import datetime
import csv
import pymongo

csvFile = sys.argv[1]
mongoURL = sys.argv[2]

risks = {
    "None": "low",
    "Critical": "high"
}

def openCSVFile():
    initPoster()
    try:
        with open(csvFile, newline='') as csvOpenedFile:
            dictReader = csv.DictReader(csvOpenedFile)
            postVulnToMongoDB(dictReader)
    except:
        printError()
    

def postVulnToMongoDB(dictReader):
    try:
        mongoConnection = mongoConnect()
        for row in dictReader:
            #Format to JSON
            vulnJSON = {
                "domain": row['Host'],
                "resource": "N/A", #target?
                "vulnerability_name": row['Name'],
                "observation": getJSONObservation(row),
                "extra_info": row['Synopsis'],
                "image_string": "N/A",
                "file_string": "N/A",
                "date_found": "N/A", #getScanDate
                "last_seen": "N/A", #getScanDate
                "language": "N/A",
                "cvss_score": row['CVSS v2.0 Base Score'],
                "vuln_type": "Infra",
                "state": "new"
            }
            print(vulnJSON)
            #addInfraVuln(mongoConnection, vulnJSON)
    except:
        printError()
    

def addInfraVuln(mongoConnection, vulnJSON):
    try:
        mongoDB = "Project"
        infraVulns = mongoConnection[mongoDB]['infra_vulnerabilities']
        exists = infraVulns.find_one({'domain': vulnJSON['domain'], 'resource': vulnJSON['resource'], 
        'vulnerability_name': vulnJSON['vulnerability_name'], 'language': vulnJSON['language']})
        if exists:
            updateVulnMongoDB(infraVulns, vulnJSON, exists)
            
        else:
            insertVulnMongoDB(infraVulns, vulnJSON)
            #AddInfraVulnToElastic
    except:
        printError()
    
def updateVulnMongoDB(infraVulns, vulnJSON, exists):
    infraVulns.update_one({'_id': exists.get('_id')}, {'$set': {
        'extra_info': vulnJSON['Synopsis'],
        'last_seen': "N/A", #getScanDate from VulnJSON
        'image_string': "N/A",
        'file_string': "N/A",
        'state': 'new' if exists['state'] != 'rejected' else exists['state']
    }})

def insertVulnMongoDB(infraVulns, vulnJSON):
    infraVulns.insert_one(vulnJSON)

def mongoConnect():
    return pymongo.MongoClient(f"mongodb://{mongoURL}/",connect=False)

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

def getScanDate(row):
    pluginOutput = row['Plugin Output']
    #Ver de convertir a json y obtener el Scan Start Date.

def initPoster():
    print("------------------------------------------")
    print("New Infra Vulns --> MongoDB Posting \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")

def successPoster():
    print("Post to MongoDB has succeeded. You may check the pertinent dashboard on Kibana\n")

def printError():
    print("---------------------------\n")
    print("Error:\n")
    print(sys.exc_info())
    
openCSVFile()