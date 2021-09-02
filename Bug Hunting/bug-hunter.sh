#!/bin/bash

# Usage: ./bug-hunter.sh [domains file list]

while true
do
  echo "bug-hunter - starting..." | slackcat -c bug-hunter -s
  ./subdomain_enum.sh $1
  sudo ./nuclei_scan.sh subdomains.txt
  sudo ./dirnfiles_enum.sh subdomains.txt
  echo "bug-hunter - done" | slackcat -c bug-hunter -s
done