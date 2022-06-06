import sys
import os
import shutil
import stat

projectsDict = dict()

def startCleaner():
    try:
        definedPathProjects = "/var/jenkins_home/workspace/"
        subfolders = [ f.name for f in os.scandir(definedPathProjects) if f.is_dir() ]
        print(subfolders)
        for folder in subfolders:
            if not folder.__contains__("tmp"):
                project = folder
                print(project)
                definedPathBuilds = f"/var/jenkins_home/jobs/{project}/builds/"
                print(definedPathBuilds)
                #subfolderBuilds = [ f.name for f in os.scandir(definedPathBuilds) if f.is_dir() and f.name.isnumeric() ]
                #Apply chmod in subfolder.
                #for subfolder in subfolderBuilds:
                #    os.chmod(subfolder,stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)
                #print(f'Project: {project}\nTotal builds: {subfolderBuilds}')
                #subfolderBuilds.remove(max(subfolderBuilds, key=int))
                #print(f'Builds to remove: {subfolderBuilds}\n')
                #projectsDict[folder] = subfolderBuilds
        #print(projectsDict)
        #deleteBuilds(projectsDict, definedPathBuilds)
    except:
        printError(sys.exc_info())
    pass

def deleteBuilds(projectsDict, definedPathBuilds):
    try:
        for directoryProject in projectsDict:
            if list.count(projectsDict[directoryProject]) > 1:
                for build in projectsDict[directoryProject]:
                    try:
                        if projectsDict[directoryProject]:
                            temporaryDir = f"{definedPathBuilds}/{build}/"
                            print(f'Removing... {temporaryDir}\n')
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
