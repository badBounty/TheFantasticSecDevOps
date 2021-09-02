#!/bin/bash

# ./nuclei_scan.sh [subdomains file list]

echo "nuclei scan - starting" | slackcat -c bug-hunter -s

mkdir $1-nuclei_tests
cd $1-nuclei_tests
echo "nuclei scan - updating templates" | slackcat -c bug-hunter -s
nuclei -update-templates

INITIALSEND="0"

FINALO=$1-final.txt
OFINALO=$1-oldfinal.txt

if [ ! -f $FINALO ] 
then
	INITIALSEND="1"
fi

echo "nuclei scan - scanning" | slackcat -c bug-hunter -s
for scan in $(cat ../nuclei-scans); do
        nuclei -l $1 -t $scan -o "$1-$scan.nuclei" -silent
        cat $1-$scan.nuclei >> $FINALO
done

if [ "${INITIALSEND}" -eq "1" ] 
then
	cat $FINALO | sort | uniq > $FINALO
	slackcat -c bug-hunter $FINALO
else
	cat $FINALO | sort | uniq > $FINALO
	if cmp --silent -- "$FINALO" "$OFINALO"; then
        	echo "nuclei scan - nothing new found." | slackcat -c bug-hunter -s
      	else
        	NEWFOUND=nuclei-newfound.txt
        	comm -23 <(sort $FINALO) <(sort $OFINALO) > $NEWFOUND
        	slackcat -c bug-hunter $NEWFOUND
        	rm $NEWFOUND
      fi
fi

cp $FINALO $OFINALO

echo "nuclei scan - done"  | slackcat -c bug-hunter -s