import re
import json
import sys

def format(issues):
    dic = {}
    for issue in issues:
        if not(issue[0] in dic):
            dic[issue[0]] = []
        dic[issue[0]].append([issue[3], issue[1], issue[2]])
    return dic


def parser(fname):
    with (open(fname, 'r')) as f:
        lines = f.readlines()
        issues = []
        for line in lines:
            l = line.split(':')
            l[0] = l[0].replace('(', ',').replace(')', ',').split(',')
            l[2] = re.sub(r' \[.*\]$', '', l[2].replace('\n', ''))
            component = l[0][0]
            affectedline = l[0][1]
            rule = l[1].replace('warning', '').strip()
            message = l[2]
            issues.append([rule, component, affectedline, message])
        return issues


if __name__ == "__main__":
    if (len(sys.argv) != 3):
        print('Parameters error')
        sys.exit()
    issues = parser(sys.argv[1])
    j = format(issues)
    json.dump(j, open(sys.argv[2], 'w'))
    pass
