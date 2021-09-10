#!/bin/bash

# ./nuclei_scan.sh [subdomains file list] [slack channel]

mkdir $1-nuclei_tests
cd $1-nuclei_tests
echo "nuclei scan - updating templates" | slackcat -c $2 -s
sudo /root/go/bin/nuclei -update-templates

INITIALSEND="0"

FINALOT=$1-nuclei-tempfinal.txt
FINALO=nuclei.txt
OFINALO=$1-nuclei-oldfinal.txt

if [ ! -f $FINALO ] 
then
	INITIALSEND="1"
fi

echo "nuclei scan - starting..." | slackcat -c $2 -s

sudo /root/go/bin/nuclei -l ../$1 -t /root/nuclei-templates -include-tags fuzz,misc -nts -o $FINALOT

if [ "${INITIALSEND}" -eq "1" ] 
then
	cat $FINALOT | sort | uniq > $FINALO
	rm $FINALOT
	slackcat -c $2 $FINALO
else
	cat $FINALOT | sort | uniq > $FINALO
	rm $FINALOT
	if cmp --silent -- "$FINALO" "$OFINALO"; then
        	echo "nuclei scan - no new results were found" | slackcat -c $2 -s
      	else
        	NEWFOUND=nuclei-newfound.txt
        	comm -23 <(sort $FINALO) <(sort $OFINALO) > $NEWFOUND
        	slackcat -c $2 $NEWFOUND
        	rm $NEWFOUND
      fi
fi

cp $FINALO $OFINALO

echo "nuclei scan - done"  | slackcat -c $2 -s
