import re
import json
import sys

def parserDC(fname, to):
    vulns = []
    try:
        with open(fname, 'r') as f:
            lines = f.readlines()[1:]
            for line in lines:
                l = line.split(',')
                try:
                    dependency = l[3]
                    Severity = l[18].upper()
                    global maxSeverity
                    if (Severity in severityDict):
                        if (severityDict[Severity] > severityDict[maxSeverity]):
                            maxSeverity = Severity
                    if (not(dependency in vulns)):
                        vulns.append(dependency)
                except:
                    print('Error')
    except:
        print('Error')     
    json.dump(vulns, open(to, 'w'), indent=2, sort_keys=True)

if __name__ == "__main__":
    if (len(sys.argv) != 4):
        print('Parameters error')
        sys.exit()
    severityDict = {
        "NULL": -1,
        "LOW":0,
        "MEDIUM":1,
        "HIGH": 2,
        "CRITICAL":3
    }
    maxSeverity = "NULL"
    parserDC(sys.argv[1], sys.argv[2])
    with open(sys.argv[3], 'w') as f:
        f.write(maxSeverity.capitalize())
