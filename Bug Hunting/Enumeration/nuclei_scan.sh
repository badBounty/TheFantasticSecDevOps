#!/bin/bash

# ./nuclei_scan.sh [subdomains file list]

if [ ! $# -eq 1 ]; then
	echo "[x] Not enough arguments or too many arguments: $0 [subdomains file list]"
	exit
elif [[ ! -f $1 ]]; then
	echo "[x] '$1' file does not exist"
	exit
elif [[ ! -f "nuclei-scans" ]]; then
	echo "[x] 'nuclei-scans' file not defined"
	exit
fi

echo "Launching Nuclei"
mkdir $1-nuclei_tests
cd $1-nuclei_tests
echo "Updating Templates"
nuclei -update-templates
echo "Testing"
for scan in $(cat ../nuclei-scans); do
        echo "Launching template: " $scan
        nuclei -l $1 -t $scan/ -o $1-$scan.nuclei
        cat $1-$scan.nuclei | slackcat --channel general --stream
done
echo "Results sent"
