import json
import sys
import datetime
import os 
import os.path

sourcePath = sys.argv[1]
outputPath = sys.argv[2]
projName = sys.argv[3]
nodeLibraries = {"Technology": "NodeJS", "Path": "", "Libraries": []}
nodeFinalJSON = []

def outputNodeLibraries(nodeFinalJSON):
    try:
        with open(outputPath,'a+') as nodeLib:
            json.dump(nodeFinalJSON,nodeLib,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def resetNodeLibraries():
    global nodeLibraries
    nodeLibraries = {"Technology": "NodeJS", "Path": "", "Libraries": []}

def initSCP(dir):
    fileJSON = open(dir,'r')
    return fileJSON

def getLibrariesJSON():
    print("------------------------------------------")
    print("SCP NodeJS - Libraries \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    try:
        for dirpath, dirnames, filenames in os.walk(sourcePath):
            for filename in [f for f in filenames if f == 'package.json']:
                dir = os.path.join(dirpath,filename)
                fileJSON = json.load(initSCP(dir))
                if 'dependencies' in fileJSON:
                    for line in fileJSON['dependencies']:
                        lib = line
                        ver = fileJSON['dependencies'][lib]
                        libJSON = {
                            'Library': line,
                            'Version': ver.replace('^','')
                        }
                        nodeLibraries['Libraries'].append(libJSON)  
                        finalDir = dir.split(f'{projName}')
                        nodeLibraries['Path'] = f"{projName}"+finalDir[1]     
                    nodeFinalJSON.append(nodeLibraries)        
                resetNodeLibraries()
        outputNodeLibraries(nodeFinalJSON)
    except:
        print("Error: ", sys.exc_info())
        pass

getLibrariesJSON()
