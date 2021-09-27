import re
import json
import sys

def parserAudit(fname, to):
    out = json.load(open(to, 'r'))
    try:
        j = json.load(open(fname,  'r'))
        for vuln in j['actions']:
            module = vuln["module"]
            version = vuln["target"]
            lib = "{}:{}".format(module, version)
            for adv in vuln["resolves"]:
                    advID = adv["id"]
                    Severity = j["advisories"][str(advID)]["severity"].upper()
                    global maxSeverity
                    if (Severity in severityDict):
                        if (severityDict[Severity] > severityDict[maxSeverity]):
                            maxSeverity = Severity
            if (not(lib in out)):
                out.append(lib)              
    except:
        print('Error')
    json.dump(out, open(to, 'w'), indent=2, sort_keys=True)

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
    parserAudit(sys.argv[1], sys.argv[2])
    with open(sys.argv[3], 'w') as f:
        f.write(maxSeverity.capitalize())
