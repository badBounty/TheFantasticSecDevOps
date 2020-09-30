import re
import json
import sys


def parser(fname, to):
    j = json.load(open(fname, 'r'))
    issues = []
    vulns = j["nodejs"]
    for vuln in vulns:
        for f in vulns[vuln]["files"]:
            issue = {
                "title": vuln,
                "lineNumber": f["match_lines"][0],
                "file": f["file_path"],
                "line": f["match_string"]
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