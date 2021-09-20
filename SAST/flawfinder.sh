#!/bin/bash
set -x
if [ "$#" != "1" ] ; then
  echo "Se esperaban 1 argumento y se recibieron $#";
  exit 1;
fi

flawfinder -c -D --csv /home/$1 > flawfinder.csv

python3 /home/parseFlawfinderResults.py /home/flawfinder.csv /home/flawfinder-results-parsed.json $1
