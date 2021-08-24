#!/bin/bash

# ./nuclei_scan.sh [subdomains file list]

echo "nuclei scan - starting" | slackcat -c bug-hunter -s

mkdir $1-nuclei_tests
cd $1-nuclei_tests
echo "nuclei scan - updating templates"  | slackcat -c bug-hunter -s
nuclei -update-templates

echo "nuclei scan - scanning"  | slackcat -c bug-hunter -s
for scan in $(cat ../nuclei-scans); do
        nuclei -l $1 -t $scan/ -o $1-$scan.nuclei
        cat $1-$scan.nuclei | slackcat -c bug-hunter -s
done

echo "nuclei scan - done"  | slackcat -c general -s
