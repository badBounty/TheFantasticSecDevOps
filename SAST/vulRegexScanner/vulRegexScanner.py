import re
import json
import sys
import os.path
import datetime

logFile = None
outputFile = None

def initLog():
    global logFile
    logFile = open("log.txt", "a")

def initOutput():
    global outputFile
    outputFile = open("result.json", "w")
    outputFile.write("[\n")

def writteResult(key, archivo, line, contenido):
    outputFile.write("{ \"title\":\"" + key +"\", \"file\":\"" + archivo + "\", \"lineNumber\":" + str(line) + ", \"line:\"" + contenido + "\"},\n")

def logError(msj):
    msj = "Error - " + msj  + "\n"
    logFile.write(msj)
    print(msj)

def logInfo(msj):
    msj = "Info - " + msj  + "\n"
    logFile.write(msj)
    print(msj)

def loadRegex():
    try:
        expresions = json.load(open("regex.json", 'r'))
        return expresions
    except:
        logError("Al cargar las expresiones del archivo regex.json")
        sys.exists(13)

def checkPathexist(source):
    if (not os.path.exists(source)):
        logError("No se ha encontrado la carpeta del codigo fuente " + source)
        sys.exit(13)

def checkParameters():
    if (len(sys.argv) != 2):
        logError("Se ha recibido mas datos o menos de los necesarios como parametros, solo se debe recibir un parametro la ruta del codigo")
        sys.exit(13)

def closeOutput():
    outputFile.write("]")

if __name__ == "__main__":
    initLog()
    logInfo("Iniciando nuevo escaneo fecha " + datetime.datetime.now().strftime("%d/%m/%Y - %H:%M:%S"))

    checkParameters()
    
    source = sys.argv[1]
    logInfo("El codigo a analizar se encuentra en la carpeta " + source)
    checkPathexist(source)
    
    logInfo("Cargando expresiones")
    expresions = loadRegex()

    logInfo("Iniciando output")
    initOutput()

    for expresion in expresions:
        key = expresion["key"]
        regex = expresion["regex"]
        regexCom = re.compile(regex)

    for r, d, f in os.walk(source):
        for archivo in f:
            fullPathFile = os.path.join(r, archivo)
            numberLine = 0
            for line in open(fullPathFile, "r"):
                numberLine = numberLine + 1
                if regexCom.match(line):
                    writteResult(key, fullPathFile, numberLine, line.replace('\n', '').replace('\r', ''))
    closeOutput()
    logInfo("Escaneo finalizado")