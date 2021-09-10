#!/bin/bash 

#Usage: ./nmap_scan.sh [subdomains file list] [slack channel]

echo "nmap scan - starting..." | slackcat -c $2 -s
nmap -sSUV -Pn -T4 -iL $1 -p `cat ports.txt` -oN subdomains_scan.nmap -oX subdomains_scan.xml
echo "nmap scan - uploading files..." | slackcat -c $2 -s
slackcat -c $2 subdomains_scan.nmap
slackcat -c $2 subdomains_scan.xml
rm subdomains_scan.nmap
rm subdomains_scan.xml
echo "nmap scan - done." | slackcat -c $2 -s
