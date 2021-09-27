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
                    print('')
    except:
        print('')
            
    json.dump(vulns, open(to, 'w'), indent=2, sort_keys=True)

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
        print('')
    json.dump(out, open(to, 'w'), indent=2, sort_keys=True)


if __name__ == "__main__":
    if (len(sys.argv) != 5):
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
    parserDC(sys.argv[1], sys.argv[3])
    parserAudit(sys.argv[2], sys.argv[3])
    with open(sys.argv[4], 'w') as f:
        f.write(maxSeverity.capitalize())
