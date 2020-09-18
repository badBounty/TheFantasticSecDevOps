import re
import json
import sys


def normalizeTitle(title):
    j = json.load(open("/home/normalization.json", 'r'))
    try:
        normalizedTitle = j[title]
        print(normalizedTitle)
    except:
        print("")


if __name__ == "__main__":
    if (len(sys.argv) > 2):
        args = sys.argv[1:]
        arg = " ".join(args)
    title = arg
    normalizeTitle(title)
    pass
