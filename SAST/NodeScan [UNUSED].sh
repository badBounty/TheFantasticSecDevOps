#!/bin/bash
set -x
if [ "$#" != "1" ] ; then
  echo "Se esperaban 1 argumentos y se recibieron $#";
  exit 1;
fi

njsscan --json -o /home/preparseoutput.json $1

python3 /home/parseNodejsscan.py /home/preparseoutput.json /home/output.json
