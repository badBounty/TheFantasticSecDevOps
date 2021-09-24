import re
import json
import sys
import base64

def parser(fname, to):
    j = json.load(open(fname, 'r'))
    issues = []
    vulns = j["nodejs"]
    for vuln in vulns:
        for f in vulns[vuln]["files"]:
            line = base64.b64encode(bytes(f["match_string"], 'utf-8'))
            issue = {
                "title": vuln,
                "lineNumber": f["match_lines"][0],
                "file": f["file_path"],
                "line": line
            }
            print(issue)
            issues.append(issue)

    json.dump(issues, open(to, 'w'))


if __name__ == "__main__":
    if (len(sys.argv) != 3):
        print('Parameters error')
        sys.exit()
    issues = parser(sys.argv[1], sys.argv[2])

    pass
