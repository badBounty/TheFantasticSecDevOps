#!/bin/bash
set -x
if [ "$#" != "1" ] ; then
  echo "Se esperaba 1 argumento y se recibieron $#";
  exit 1;
fi

npm --prefix $1 audit --json > /home/npmaudit.json

python3 /home/parseNPMAuditResults.py /home/npmaudit.json /home/output.json /home/severity.txt
