#!/bin/bash

# ./nuclei_scan.sh [subdomains file list]

echo "nucle scan - starting" | slackcat -c general -s

mkdir $1-nuclei_tests
cd $1-nuclei_tests
echo "nucle scan - updating templates"  | slackcat -c general -s
nuclei -update-templates

echo "nucle scan - scaning"  | slackcat -c general -s
for scan in $(cat ../nuclei-scans); do
        nuclei -l $1 -t $scan/ -o $1-$scan.nuclei
        cat $1-$scan.nuclei | slackcat --channel general --stream
done

echo "nuclei scan - ending"  | slackcat -c general -s