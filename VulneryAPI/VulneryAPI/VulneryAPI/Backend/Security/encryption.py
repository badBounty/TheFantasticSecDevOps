from VulneryAPI.Backend import logger
from VulneryAPI.settings import LOGIN, LOGGER_APP_NAMES

import hashlib
import secrets
import sys

#Computes SHA256 hash 

def computeSHA256Hash(input):
    try:
        salt = generateToken(16)
        hashedResult = hashlib.sha256(salt.encode()+str(input).encode()).hexdigest()
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],f"hash: {hashedResult}  salt: {salt}")
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())

#Generates random token with provided length

def generateToken(number):
    try:
        return secrets.token_hex(number)
    except:
        logger.logError(LOGGER_APP_NAMES['VulnsPoster'],sys.exc_info())