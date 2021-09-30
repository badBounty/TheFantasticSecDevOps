import json
import sys
import datetime
import os 
import os.path

sourcePath = sys.argv[1]
outputPath = sys.argv[2]
projName = sys.argv[3]
nodeLibraries = {"Technology": "NodeJS", "Path": "", "Libraries": []}

def outputNodeLibraries(nodeLibraries):
    try:
        with open(outputPath,'a+') as nodeLib:
            json.dump(nodeLibraries,nodeLib,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def initSCP(dir):
    print("------------------------------------------")
    print("SCP NodeJS - Libraries \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    fileJSON = open(dir,'r')
    return fileJSON

def getLibrariesJSON():
    try:
        for dirpath, dirnames, filenames in os.walk(sourcePath):
            for filename in [f for f in filenames if f == 'package.json']:
                dir = os.path.join(dirpath,filename)
                fileJSON = json.load(initSCP(dir))
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
        outputNodeLibraries(nodeLibraries)
    except:
        print("Error: ", sys.exc_info())
        pass

getLibrariesJSON()
