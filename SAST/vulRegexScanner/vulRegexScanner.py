import re
import json
import sys
import os.path
import datetime
import traceback
import codecs

outputReportName = "result.json"
logFile = None
outputFile = None
extensiones =  ('.js', '.cs', '.env', '.txt', '.java','.sh','.git-credentials')
vuls = {}

def initLog():
    global logFile
    logFile = open("log.txt", "a")

def initOutput():
    global outputFile
    outputFile = open(outputReportName, "w")
    outputFile.write('{"results":')
    outputFile.write("[\n")

def writteResult(key, archivo, line, contenido):
    outputFile.write("{ \"title\":\"" + key +"\", \"file\":\"" + archivo + "\", \"lineNumber\":" + str(line) + ", \"line\":\"" + contenido + "\"},\n")

def crearEntVul(archivo, line, contenido):
    return "{ \"file\":\"" + archivo + "\", \"lineNumber\":" + str(line) + ", \"line\":\"" + contenido + "\"}"

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
    outputFile.write("}")
    outputFile.flush()

def clean_json(string):
    string = re.sub(",[ \t\r\n]+}", "}", string)
    string = re.sub(",[ \t\r\n]+\]", "]", string)
    return string

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
        case = expresion["caseSensitive"]
        try:
            for r, d, f in os.walk(source):
                for archivo in f:
                    if archivo.endswith(extensiones):
                        fullPathFile = os.path.join(r, archivo)
                        numberLine = 0
                        for line in codecs.open(fullPathFile, 'r', encoding='utf-8'):
                            numberLine = numberLine + 1

                            nombreVul= key
                            rutaCompleta = fullPathFile.replace('\\', '/')
                            numLinea = numberLine
                            lineaAf = line.replace('\n', '').replace('\r', '').replace('"', '\'').replace('\\', '/')

                            if (case == "false"):
                                if re.match(regex, line, re.IGNORECASE):
                                    if nombreVul not in vuls: vuls[nombreVul] = []
                                    vuls[nombreVul].append(crearEntVul(rutaCompleta, numLinea, lineaAf))
                            else:
                                if re.match(regex, line):
                                    if nombreVul not in vuls: vuls[nombreVul] = []
                                    vuls[nombreVul].append(crearEntVul(rutaCompleta, numLinea, lineaAf))
        except:
            tb = traceback.format_exc()
            logFilePath = ""
            if fullPathFile != None:
                logFilePath = fullPathFile
            logError("Expresion:" + key + " - Archivo:" + logFilePath + " - Stacktrace:" + tb)
            sys.exit()

    logInfo("Escribiendo output")

    for item in vuls:
        vulne = item
        recu = vuls[item]

    closeOutput()

    logInfo("Escaneo finalizado")