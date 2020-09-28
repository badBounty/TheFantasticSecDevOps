import re
import json
import sys
import pandas as pd
import numpy as np

def parserDC(fname, to):
    df = pd.read_csv(fname).replace(np.nan, '', regex=True)
    data = df.drop(columns=['Project', 'ScanDate', 'License', 'CVSSv3', 'CVSSv2', 'CPE Confidence', 'CPE', 'Md5', 'Sha1', 'Identifiers', 'Description', 'Source', 'Evidence Count'])

    vulns = {}
    for ind in data.index:
        if (data['CVE'][ind] != ""):
            if (data['DependencyName'][ind] in vulns):
                vulns[data['DependencyName'][ind]].append(data['CVE'][ind])
            else:
                vulns[data['DependencyName'][ind]] = [data['CVE'][ind]]
        elif (data['CWE'][ind] != ""):
            if (data['DependencyName'][ind] in vulns):
                vulns[data['DependencyName'][ind]].append(data['CWE'][ind])
            else:
                vulns[data['DependencyName'][ind]] = [data['CWE'][ind]]
    json.dump(vulns, open(to, 'w'), indent=2, sort_keys=True)

def parserAudit(fname, to):
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


if __name__ == "__main__":
    if (len(sys.argv) != 4):
        print('Parameters error')
        sys.exit()
    parserDC(sys.argv[1], sys.argv[3])
    parserAudit(sys.argv[2], sys.argv[3])
