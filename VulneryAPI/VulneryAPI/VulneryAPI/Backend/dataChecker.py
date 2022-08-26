from operator import truediv
from VulneryAPI.Backend import logger
from VulneryAPI.settings import LOGIN, LOGGER_APP_NAMES
import json
import sys

def checkLoginData(loginJSON):
    try:
        loginJSONProcessed = json.loads(loginJSON)
        for key in loginJSONProcessed:
            if key not in LOGIN:
                return False
        return True   
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())
