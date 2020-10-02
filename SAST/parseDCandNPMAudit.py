import re
import json
import sys



def parserDC(fname, to):
    vulns = {}
    with open(fname, 'r') as f:
        lines = f.readlines()[1:]
        for line in lines:
            l = line.split(',')
            try:
                dependency = l[3]
                cve_cwe = l[11] if l[11] != '' else l[12]
                if (dependency in vulns):
                    vulns[dependency].append(cve_cwe)
                else:
                    vulns[dependency] = [cve_cwe]
            except:
                print('')
            
    json.dump(vulns, open(to, 'w'), indent=2, sort_keys=True)

def parserAudit(fname, to):
    try:
        j = json.load(open(fname,  'r'))
        out = json.load(open(to, 'r'))
        for vuln in j['actions']:
            module = vuln["module"]
            version = vuln["target"]
            lib = "{}:{}".format(module, version)
            for resolve in vuln["resolves"]:
                if lib in out:
                    out[lib].append(resolve["id"])
                else:
                    out[lib] = [resolve["id"]]
        json.dump(out, open(to, 'w'), indent=2, sort_keys=True)
    except:
        print('')


if __name__ == "__main__":
    if (len(sys.argv) != 4):
        print('Parameters error')
        sys.exit()
    parserDC(sys.argv[1], sys.argv[3])
    parserAudit(sys.argv[2], sys.argv[3])
