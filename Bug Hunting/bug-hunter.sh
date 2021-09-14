#!/bin/bash

# Usage: ./bug-hunter.sh [domains file list] [slack channel]

while true
do
  echo "bug-hunter - starting..." | slackcat -c $2 -s
  ./subdomain_enum.sh $1 $2
  sudo ./nmap_scan.sh subdomains_np.txt $2
  sudo ./nuclei_scan.sh subdomains.txt $2
  sudo ./dirnfiles_enum.sh subdomains.txt $2
  echo "bug-hunter - done" | slackcat -c $2 -s
done
