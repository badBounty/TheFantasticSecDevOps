from xml.dom import minidom
import json
import sys
import datetime
import os 
import os.path

sourcePath = sys.argv[1]
outputPath = sys.argv[2]
projName = sys.argv[3]
mavenLibraries = {"Technology": "Maven", "Path": "", "Libraries": []}
mavenFinalJSON = []

def outputMavenLibraries(mavenFinalJSON):
    try:
        with open(outputPath,'a+') as mavenLib:
            json.dump(mavenFinalJSON,mavenLib,ensure_ascii=False)
        print("Success: Proceso finalizado.")
    except:
        print("Error: no se pudo escribir el resultado.")
        pass

def resetMavenLibraries():
    global mavenLibraries
    mavenLibraries = {"Technology": "Maven", "Path": "", "Libraries": []}

def initSCA(dir):
    file = open(dir,'r')
    return file

def fillJSON(lib,ver):
    libJSON = {
        'Library': lib,
        'Version': ver
    }
    return libJSON

def getLibrariesXML():
    print("------------------------------------------")
    print("SCA Maven - Libraries \n")
    print("Date: " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S")+"\n")
    try:
        for dirpath, dirnames, filenames in os.walk(sourcePath):
            for filename in [f for f in filenames if f == 'pom.xml']:
                dir = os.path.join(dirpath,filename)
                fileXML = minidom.parse(initSCA(dir))
                dependencies = fileXML.getElementsByTagName('dependencies')
                if dependencies:
                    for elem in dependencies:
                        dependency = elem.getElementsByTagName('dependency')
                        for data in dependency:
                            lib = data.getElementsByTagName('artifactId')
                            ver = data.getElementsByTagName('version')
                            if ver and data:
                                libJSON = fillJSON(lib[0].firstChild.nodeValue,ver[0].firstChild.nodeValue)
                            else:
                                libJSON = fillJSON(lib[0].firstChild.nodeValue,'No especifica')
                            mavenLibraries['Libraries'].append(libJSON)
                            finalDir = dir.split(f'{projName}')
                            mavenLibraries['Path'] = f"{projName}"+finalDir[2]  
                    mavenFinalJSON.append(mavenLibraries)        
                resetMavenLibraries()
        outputMavenLibraries(mavenFinalJSON)            
    except:
        exc_type, exc_obj, exc_tb = sys.exc_info()
        fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
        print(sys.exc_info(), fname, exc_tb.tb_lineno)
        pass

getLibrariesXML()
