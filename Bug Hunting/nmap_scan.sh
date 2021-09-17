#!/bin/bash 

#Usage: ./nmap_scan.sh [subdomains file list] [slack channel]

SCRIPTS="banner,vuln,vulscan/vulscan.nse,http-enum,http-webdav-scan,http-backup-finder,http-trace,http-config-backup,http-wordpress-enum,http-rfi-spider,http-cors,http-cookie-flags,http-waf-detect,http-apache-server-status,http-bigip-cookie,http-devframework,http-git"

echo "nmap scan - starting..." | slackcat -c $2 -s
nmap -sSUV --script=$SCRIPTS -Pn -T4 -iL $1 -p `cat ports.txt` -oN subdomains_scan.nmap -oX subdomains_scan.xml
echo "nmap scan - parsing results..." | slackcat -c $2 -s
python3 nmaptocsv.py -x subdomains_scan.xml -f fqdn-port-service -d ":" -s -n | tr -d '"' | grep -e ":http" -e ":https" | grep -v ":http-" | grep -v ":https-" | grep -v ":80:" | grep -v ":81:" | grep -v ":443:" | grep -v ":591:" | grep -v ":2082:" | grep -v ":2087:" | grep -v ":2095:" | grep -v ":2096:" | grep -v ":3000:" | grep -v ":8000:" | grep -v ":8001:" | grep -v ":8008:" | grep -v ":8080:" | grep -v ":8083:" | grep -v ":8443:" | grep -v ":8834:" | grep -v ":8888:" | awk -F: '(NR==0){h1=$1;h2=$2;h3=$3;next} {print $3"://"$1":"$2"/"}' > subdomains_portsfound.txt
echo "nmap scan - uploading files..." | slackcat -c $2 -s
slackcat -c $2 subdomains_scan.nmap
slackcat -c $2 subdomains_scan.xml
if [ -s subdomains_portsfound.txt ]; then
	slackcat -c $2 subdomains_portsfound.txt
else
	echo "nothing relevant found about differences between aquatone and nmap" | slackcat -c $2 -s
fi
rm subdomains_portsfound.txt
rm subdomains_scan.nmap
rm subdomains_scan.xml
echo "nmap scan - done." | slackcat -c $2 -s
