import sys
import os
import shutil

project = ""
definedPathBuilds = f"/var/jenkins_home/jobs/{project}/builds/"
definedPathProjects = "/var/jenkins_home/workspace/"
projectsDict = dict()

def startCleaner():
    try:
        os.chdir(definedPathProjects)
        subfolders = [ f.path for f in os.scandir(definedPathProjects) if f.is_dir() ]
        for folder in subfolders:
            if not folder.__contains__("tmp"):
                #projectsDict[folder] = []
                global project 
                project = folder
                os.chdir(definedPathBuilds)
                subfolderBuilds = [ f.path for f in os.scandir(definedPathBuilds) if f.is_dir() and f.name.isnumeric() ]
                subfolderBuilds.remove(max(subfolderBuilds))
                projectsDict[folder] = subfolderBuilds
        deleteBuilds(projectsDict)
    except:
        printError(sys.exc_info())
    pass

def deleteBuilds(projectsDict):
    try:
        for directoryProject in projectsDict:
            global project
            project = directoryProject
            os.chdir(definedPathBuilds)
            for build in projectsDict[directoryProject]:
                shutil.rmtree(f"{definedPathBuilds}/{build}/", ignore_errors=True)
    except:
        printError(sys.exc_info())
    pass

def printError(exception):
    print(f"The cleaning couldn't be done. Reason of failure: {exception}")

startCleaner()
