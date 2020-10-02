#!/bin/bash
if [ "$#" != "3" ] ; then
  echo "Se esperaban 3 argumentos y se recibieron $#";
  exit 1;
fi

njsscan --json -o /home/preparseoutput.json $1

python3 /home/parseNodejsscan.py /home/preparseoutput.json /home/output.json