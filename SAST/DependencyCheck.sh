#!/bin/bash
set -x
if [ "$#" != "2" ] ; then
  echo "Se esperaban 2 argumentos y se recibieron $#";
  exit 1;
fi

/home/dependency-check/dependency-check/bin/dependency-check.sh --project $2 --scan $1 --format CSV -o '/home/dependency-check.csv'
