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

IFS=$'\n'
set -f

for domain in $(cat < $1); do
# $1 = [domain] ? = [output?] ? = [app_path?]
	nmap-parse-output 'tcp.top1000.xml' service '{0}' | cut -d ':' -f2 > parsed_tcp_ports.txt

	PARSED=parsed_tcp_ports.txt
	while read $port; do
        	case $port in
                	"53")
                        	nmap -Pn -sSV -p $port --script=vuln,vulscan/vulscan.nse,banner,dns-cache-snoop,dns-recursion -oA "_OUTPUT_/dns/$domain.$port.script" $domain
                	;;
                	"21")
                        	nmap -Pn -sSV -p $port --script=vuln,vulscan/vulscan.nse,banner,ftp-anon,ftp-bounce,ftp-syst -oA "_OUTPUT_/ftp/$domain.$port.script" $domain
                        	#ncrack -v -U "_APP_PATH_/brute-force/usernames.ftp.txt" -P "_APP_PATH_/brute-force/passwords.ftp.txt" -p ftp:$port -oA "_OUTPUT_/ftp/$domain.$port.ncrack" $domain
                	;;
                	"3389")
                        	nmap -Pn -sSV -p $port --script=vuln,vulscan/vulscan.nse,banner,rdp-ntlm-info,rdp-vuln-ms12-020 -oA "_OUTPUT_/rdp/$domain.$port.script" $domain
                        	#ncrack  -v -U "_APP_PATH_/brute-foce/usernames.rdp.txt" -P "_APP_PATH_/brute-force/passwords.rdp.txt" -p rdp:{1} -oA "_OUTPUT_/rdp/$domain.$port.ncrack" $domain
                	;;
                	"139" | "445")
                        	nmap -Pn -sSV -p $port --script=vuln,vulscan/vulscan.nse,smb-enum-users,banner,smb-protocols,smb-enum-shares,smb-vuln-ms17-010 -oA "_OUTPUT/smb/$domain.$port.script" $domain
                	;;
                	"25")
                        	nmap -Pn -sSV -p $port --script=vuln,vulscan/vulscan.nse,banner,smtp-open-relay,smtp-commands -oA "_OUTPUT_/smtp/$domain.$port.script" $domain
                        	smtp-user-enum -U "_APP_PATH_/brute-force/common_usernames.small.txt" -M VRFY -t $domain -p $port > "_OUTPUT_/smtp/$domain.$port.smtp-user-enum.txt"
                        	OpenRelayMagic -T _THREADS_ -t $domain -p $port -o  "_OUTPUT_/smtp/$domain.$port.openRelayMagic.txt"
                	;;
                	"22")
                        	nmap -Pn -sSV -p $port --script=vuln,vulscan/vulscan.nse,banner,ssh-auth-methods,sshv1 -oA "_OUTPUT_/ssh/$domain.$port.script" $domain
                        	ssh-audit.py $domain:$port > "_OUTPUT_/ssh/$domain.$port.sshaudit.txt"
                        	sshUsernameEnumExploit.py  --threads _THREADS_ --port $port --userList "_APP_PATH_/brute-force/common_usernames.small.txt" --outputFormat list --outputFile "_OUTPUT_/ssh/$domain.$port.sshUsernameEnumExploit" $domain
                        	#ncrack  -v -U "_APP_PATH_/brute-force/usernames.ssh.txt" -P "_APP_PATH_/brute-force/passwords.ssh.txt" -p ssh:$port -oA "_OUTPUT_/ssh/$domain.$port.ncrack" $1
                	;;
                	"23")
                        	nmap -Pn -sSV -p $port --script=vuln,vulscan/vulscan.nse,banner,telnet-ntlm-info -oA "_OUTPUT_/telnet/$domain.$port.script" $domain
                        	#ncrack  -v -U "_APP_PATH_/brute-force/usernames.telnet.txt" -P "_APP_PATH_/brute-force/passwords.telnet.txt" -p ssh:$port -oA "_OUTPUT_/telnet/$domain.$port.ncrack" $domain
                	;;
        	esac
	done < $PARSED
done



echo "nmap scan - done." | slackcat -c $2 -s
