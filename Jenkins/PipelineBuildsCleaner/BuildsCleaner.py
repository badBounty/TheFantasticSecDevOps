import sys
import os
import shutil
import stat

projectsDict = dict()

def startCleaner():
    try:
        definedPathProjects = "/var/jenkins_home/workspace/"
        subfolders = [ f.name for f in os.scandir(definedPathProjects) if f.is_dir() ]
        for folder in subfolders:
            if not folder.__contains__("tmp"):
                project = folder
                definedPathBuilds = f"/var/jenkins_home/jobs/{project}/builds/"
                subfolderBuilds = [ f.name for f in os.scandir(definedPathBuilds) if f.is_dir() and f.name.isnumeric() ]
                #Apply chmod in subfolder.
                for subfolder in subfolderBuilds:
                    os.chmod(f'{definedPathBuilds}{subfolder}',stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)
                print(f'Project: {project}\nBuilds: {subfolderBuilds}')
                subfolderBuilds.remove(max(subfolderBuilds, key=int))
                print(f'Builds to remove: {subfolderBuilds}\n')
                projectsDict[folder] = subfolderBuilds
        deleteBuilds(projectsDict)
    except:
        printError(sys.exc_info())
    pass

def deleteBuilds(projectsDict):
    try:
        for directoryProject in projectsDict:
            for build in projectsDict[directoryProject]:
                try:
                    if projectsDict[directoryProject] and build:
                        temporaryDir = f"/var/jenkins_home/jobs/{directoryProject}/builds/{build}/"
                        print(f'Removing... Project: {directoryProject} Build: {build}\n')
                        #shutil.rmtree(temporaryDir, ignore_errors=True, onerror=None)
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
