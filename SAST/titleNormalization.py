import re
import json
import sys


def normalizeTitle(title):
    j = json.load(open("normalization.json", 'r'))
    try:
        normalizedTitle = j[title]
        print(normalizedTitle)
    except:
        print("Not a normalized title")


if __name__ == "__main__":
    if (len(sys.argv) != 2):
        print('Parameters error')
        sys.exit()
    title = sys.argv[1]
    normalizeTitle(title)
    pass
