#!/bin/bash

# ./nuclei_scan.sh [subdomains file list]

mkdir $1-nuclei_tests
cd $1-nuclei_tests
echo "nuclei scan - updating templates" | slackcat -c bug-hunter -s
sudo /root/go/bin/nuclei -update-templates

INITIALSEND="0"

FINALOT=$1-nuclei-tempfinal.txt
FINALO=nuclei.txt
OFINALO=$1-nuclei-oldfinal.txt

if [ ! -f $FINALO ] 
then
	INITIALSEND="1"
fi

echo "nuclei scan - starting..." | slackcat -c bug-hunter -s

for scan in $(cat ../nuclei-scans.txt); do
	echo $scan
        sudo /root/go/bin/nuclei -l ../$1 -t "$scan" -o "$1-$scan.nuclei"
        cat "$1-$scan.nuclei" >> $FINALOT
done

if [ "${INITIALSEND}" -eq "1" ] 
then
	cat $FINALOT | sort | uniq > $FINALO
	rm $FINALOT
	slackcat -c bug-hunter $FINALO
else
	cat $FINALOT | sort | uniq > $FINALO
	rm $FINALOT
	if cmp --silent -- "$FINALO" "$OFINALO"; then
        	echo "nuclei scan - no new results were found" | slackcat -c bug-hunter -s
      	else
        	NEWFOUND=nuclei-newfound.txt
        	comm -23 <(sort $FINALO) <(sort $OFINALO) > $NEWFOUND
        	slackcat -c bug-hunter $NEWFOUND
        	rm $NEWFOUND
      fi
fi

cp $FINALO $OFINALO

echo "nuclei scan - done"  | slackcat -c bug-hunter -s