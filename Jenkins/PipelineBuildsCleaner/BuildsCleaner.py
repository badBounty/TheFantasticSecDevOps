import sys
import os
import shutil
import stat

projectsDict = dict()

def startCleaner():
    try:
        definedPathJobs = "/var/jenkins_home/jobs/"
        subfolders = [ f.name for f in os.scandir(definedPathJobs) if f.is_dir() ]
        if subfolders:
            print("\nJobs to remove:\n")
            for folder in subfolders:
                if not folder.__contains__("tmp"):
                    project = folder
                    definedPathBuilds = f"{definedPathJobs}{project}/builds/"
                    subfolderBuilds = [ f.name for f in os.scandir(definedPathBuilds) if f.is_dir() and f.name.isnumeric() ]
                    for subfolder in subfolderBuilds:
                        setCHMOD(definedPathBuilds, subfolder)
                    print(f'Project: {project}\nBuilds: {sorted(subfolderBuilds, key=int)}')
                    if subfolderBuilds:
                        subfolderBuilds.remove(max(subfolderBuilds, key=int))
                    print(f'Builds to remove: {sorted(subfolderBuilds, key=int)}\n')
                    projectsDict[folder] = subfolderBuilds
            deleteBuilds(projectsDict, definedPathJobs)
        else:
            print("There are no jobs to remove.\n")
            pass
        removeWorkspaces()
    except:
        printError(sys.exc_info())
    pass

def deleteBuilds(projectsDict, definedPathJobs):
    try:
        for directoryProject in projectsDict:
            for build in projectsDict[directoryProject]:
                try:
                    if projectsDict[directoryProject] and build:
                        temporaryDir = f"{definedPathJobs}{directoryProject}/builds/{build}/"
                        print(f'Removing - Project: {directoryProject} Build: {build}...\n')
                        shutil.rmtree(temporaryDir, ignore_errors=True, onerror=None)
                except:
                    printError(sys.exc_info())
                    printProjectError(directoryProject, build)
    except:
        printError(sys.exc_info())
    pass

def setCHMOD(definedPath, subf):
    os.chmod(f'{definedPath}{subf}',stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)

def removeWorkspaces():
    definedPathProjects = "/var/jenkins_home/workspace/"
    try:
        subfolderWorkspace = [ f.name for f in os.scandir(definedPathProjects) if f.is_dir() ]
        if subfolderWorkspace:
            print(f"\nWorkspaces to remove: {subfolderWorkspace}\n")
            for subfolder in subfolderWorkspace:
                setCHMOD(definedPathProjects, subfolder)
                print(f"Removing - Workspace: {subfolder}...\n")
                try:
                    if subfolder:
                        shutil.rmtree(f'{definedPathProjects}{subfolder}', ignore_errors=True, onerror=None)
                except:
                    printProjectError(subfolder,"")
        else:
            print("There are no workspaces to remove.\n")
            pass
    except:
        printError(sys.exc_info())
    pass

def printError(exception):
    print(f"The cleaning couldn't be done. Reason of failure: {exception}\n")

def printProjectError(project, build):
    print(f"Project error: {project}. Build: {build}\n")

startCleaner()
