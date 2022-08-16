from VulnsPoster.models import *
from VulneryAPI.Backend import logger
from VulneryAPI.settings import LOGGER_APP_NAMES

from curses.ascii import isdigit
import json
import sys 
import datetime
import csv

#Parses CSV file

def parseCSVFile(csvFile):
    try:
        dictReader = csv.DictReader(csvFile)
        return dictReader
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

def convertCSVRowToVuln(dict):
    try:
        for row in dict:
            vulnInfra = VulnInfra("",
                row['Name'],
                row['Description'],
                row['Synopsis'] if row['Synopsis'] is not None else "-",
                row['Host']
            )
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())