import re
import json
import sys




def parser(fname):
    with (open(fname, 'r')) as f:
        lines = f.readlines()
        issues = []
        for line in lines:
            if (('warning SEC' in line) or ('warning SCS' in line)):
                issue = {}
                line = line[7:]
                l = line.split(': ')
                l[0] = l[0].replace('(', ',').replace(')', ',').split(',')
                l[2] = re.sub(r' \[.*\]$', '', l[2].replace('\n', ''))
                component = l[0][0]
                affectedline = l[0][1]
                rule = l[1].replace('warning', '').strip()
                message = l[2]
                issue["title"] = rule
                issue["message"] = message
                issue["lineNumber"] = affectedline
                issue["file"] = component
                issues.append(issue)
        return issues


if __name__ == "__main__":
    if (len(sys.argv) != 3):
        print('Parameters error')
        sys.exit()
    issues = parser(sys.argv[1])
    print(issues)
    json.dump(issues, open(sys.argv[2], 'w'))
    pass