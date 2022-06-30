import logging
import json_logging

#Defines logger app name

def defineLogger(appName):
    return logging.getLogger(appName)

#Logs info

def logInfo(appName, toLog):
    logger = defineLogger(appName)
    logger.info(toLog)

#Logs JSON

def logJson(appName, toLog):
    logger = defineLogger(appName)
    #LOG JSON

#Sets logger level

def setLoggerLevel(levelName):
    if levelName == 'INFO':
        logging.basicConfig(level=logging.INFO)
    elif levelName == 'WARNING':
        logging.basicConfig(level=logging.WARNING)
    elif levelName == 'ERROR':
        logging.basicConfig(level=logging.ERROR)
    else:
        logging.basicConfig(level=logging.DEBUG)

#setLoggerLevel()
json_logging.ENABLE_JSON_LOGGING = True