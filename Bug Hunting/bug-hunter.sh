#!/bin/bash

# Usage: ./bug-hunter.sh [domains file list] [slack channel]

while true
do
  echo "bug-hunter - starting..." | slackcat -c $2 -s
  sudo ./subdomain_enum.sh $1 $2 subdomains_np.txt
  sudo ./dirnfiles_enum.sh subdomains_p.txt $2 content.txt
  sudo ./nmap_scan.sh subdomains_np.txt $2
  sudo ./nuclei_scan.sh subdomains_p.txt $2
  sudo ./javascript_scan subdomains_p.txt $2

  IFS=$'\n'
  set -f
  for subdomain in $(cat < subdomains_np.txt); do
  	sudo./Web\ Scanner/web_scanner.sh $subdomain $2
  done

  echo "bug-hunter - done" | slackcat -c $2 -s
done
