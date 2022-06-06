import sys
import os
import shutil
import stat

project = ""
definedPathBuilds = f"/var/jenkins_home/jobs/{project}/builds/"
definedPathProjects = "/var/jenkins_home/workspace/"
projectsDict = dict()

def startCleaner():
    try:
        subfolders = [ f.name for f in os.scandir(definedPathProjects) if f.is_dir() ]
        for folder in subfolders:
            if not folder.__contains__("tmp"):
                global project 
                project = folder
                definedPathBuilds = f"/var/jenkins_home/jobs/{project}/builds/"
                subfolderBuilds = [ f.name for f in os.scandir(definedPathBuilds) if f.is_dir() and f.name.isnumeric() ]
                #Apply chmod in folders.
                os.chmod(subfolderBuilds,stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)
                subfolderBuilds.remove(max(subfolderBuilds, key=int))
                projectsDict[folder] = subfolderBuilds
        print(projectsDict)
        deleteBuilds(projectsDict, definedPathBuilds)
    except:
        printError(sys.exc_info())
    pass

def deleteBuilds(projectsDict, definedPathBuilds):
    try:
        for directoryProject in projectsDict:
            global project
            project = directoryProject
            if list.count(projectsDict[directoryProject]) > 1:
                for build in projectsDict[directoryProject]:
                    try:
                        if projectsDict[directoryProject]:
                            temporaryDir = f"{definedPathBuilds}/{build}/"
                            #Apply chmod in folders.
                            os.chmod(temporaryDir,stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)
                            shutil.rmtree(temporaryDir, ignore_errors=True, onerror=None)
                    except:
                        printError(sys.exc_info())
        #removeWorkspaces()
    except:
        printError(sys.exc_info())
    pass

def removeWorkspaces():
    try:
        #Remove workspaces.
        shutil.rmtree("/var/jenkins_home/workspace/", ignore_errors=True, onerror=None)
    except:
        printError(sys.exc_info())
    pass

def printError(exception):
    print(f"The cleaning couldn't be done. Reason of failure: {exception}")

startCleaner()
