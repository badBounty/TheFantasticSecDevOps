#!/bin/bash
if [ "$#" != "2" ] ; then
  echo "Se esperaban 2 argumentos y se recibieron $#";
  exit 1;
fi

npm --prefix $1 audit --json > /home/npmaudit.json

/home/dependency-check/dependency-check/bin/dependency-check.sh --project \'$2\' --scan \'$1\' --format CSV -o '/home/dependency-check.csv'

python3 /home/parseDCandNPMAudit.py /home/dependency-check.csv /home/npmaudit.json /home/output.json