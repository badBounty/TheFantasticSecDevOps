import re
import json
import sys
import logging
import sys


def setup_logger(name, log_file, level=logging.DEBUG, console=True):
    """To setup as many loggers as you want"""
    
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    handler = logging.FileHandler(log_file)        
    handler.setFormatter(formatter)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    logger.addHandler(handler)
    if console:
        logger.addHandler(logging.StreamHandler(sys.stdout))

    return logger

def normalizeTitle(title):
    logger = setup_logger("vulnsNormalization", "/home/titleNormalization.log", console=False)
    j = json.load(open("/home/normalization.json", 'r'))
    try:
        normalizedTitle = j[title]
        print(normalizedTitle)
    except:
        logger.info("Vuln not whitelisted: {}".format(title))
        print("")


if __name__ == "__main__":
    if (len(sys.argv) > 2):
        args = sys.argv[1:]
        arg = " ".join(args)
    else:
        arg = sys.argv[1]
    title = arg
    normalizeTitle(title)
    pass
