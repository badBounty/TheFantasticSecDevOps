#!/bin/bash

# Usage: ./bug-hunter.sh [domains file list]

while true
do
  echo "bug-hunter - starting" slackcat -c general -s
  ./subdomain_enum.sh $1
  ./nuclei_scan.sh subdomains.txt
  ./dirnfiles_enum.sh subdomains.txt
  echo "bug-hunter - done" slackcat -c general -s
done