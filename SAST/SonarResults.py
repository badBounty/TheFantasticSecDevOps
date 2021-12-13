import requests
import sys
import json
import datetime

sonarUser = sys.argv[1]
sonarPass = sys.argv[2]
sonarURL = sys.argv[3]
sonarPort = sys.argv[4]
outputPath = sys.argv[5]
sonarFinalJSON = []

session = requests.session()

def sonarLogin():
    try:
        global session
        pload = {'login':sonarUser,'password':sonarPass}
        req = session.post(f'http://{sonarURL}:{sonarPort}/api/authentication/login',data=pload)
        if(req.status_code==200):
            cookies = req.cookies.get_dict()
            sonarResults(session,cookies)
        else:
            print(f"Request status code: {req.status_code}. Process aborted.")
    except:
        print("---------------------------\n")
        print("Error:\n")
        print(sys.exc_info())

def sonarResults(recievedSession, recievedCookies):
    try:
        pload = {'Cookie':f'XSFR-TOKEN={recievedCookies["XSRF-TOKEN"]}; JWT-SESSION={recievedCookies["JWT-SESSION"]}'}
        req = recievedSession.get(f'http://{sonarURL}:{sonarPort}/api/issues/search?p=1',data=pload)
        if(req.status_code==200):
            sonarResultsJSON = req.json()
            sonarParse(sonarResultsJSON)
        else:
            print(f"Request status code: {req.status_code}. Process aborted.")
    except:
        print("---------------------------\n")
        print("Error in Sonar Results request\n")
        print(sys.exc_info())

def sonarParse(recievedSonarResultsJSON):
    try:
        initParser()
        sonarIssues = recievedSonarResultsJSON["issues"]
        for vuln in sonarIssues:
            line = "N/A"
            if "textRange" in vuln:
                line = vuln["textRange"]["startLine"]
            sonarJSONFormat = {
                'title': vuln["message"],
                'component': vuln["component"],
                'severity': vuln["severity"],
                'affectedCode' : "N/A",
                'line' : line
            }
            print(sonarJSONFormat)
            sonarFinalJSON.append(sonarJSONFormat)
        outputSonarResults(sonarFinalJSON)
    except:
        print("---------------------------\n")
        print("Error in Sonar Results parsing\n")
        print(sys.exc_info())

def outputSonarResults(recievedSonalFinalJSON):
    try:
        with open(outputPath,'w') as sonarJSON:
            json.dump(recievedSonalFinalJSON,sonarJSON,ensure_ascii=False)
        print("\nSuccess: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def initParser():
    print("------------------------------------------")
    print("New SonarResults JSON Parsing \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")

sonarLogin()
