#!/bin/bash
set -x
if [ "$#" != "1" ] ; then
  echo "Se esperaba 1 argumento y se recibieron $#";
  exit 1;
fi

mvn dependency:tree -DoutputType=dot | grep \> | cut -d\> -f2 > scaMaven-$1.txt
