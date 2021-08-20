#!/bin/bash

# ./subdomain_enum.sh [domains list]

if [ ! $# -eq 1 ]; then
        echo "[x] Not enough arguments or too many arguments: $0 [domains list]"
        exit
fi

checkprogram() {
        if ! command -v "$1" &> /dev/null
        then
                echo "Error: $1 program could not be found"
                echo "Installing: $1"
                if [ "$1" == "nuclei" ]; then
                        GO111MODULE=on go get -v github.com/projectdiscovery/nuclei/v2/cmd/nuclei
                elif [ "$1" == "amass" ]; then
                        brew tap caffix/amass
                        brew install amass
                elif [ "$1" == "aquatone" ]; then
                        echo "[x] Could not install aquatone, please check: 'https://github.com/michenriksen/aquatone' for more instructions."
                        exit
                elif [ "$1" == "slackcat" ]; then
                        curl -Lo slackcat https://github.com/bcicen/slackcat/releases/download/1.7.2/slackcat-1.7.2-$(uname -s)-amd64
                        mv slackcat /usr/local/bin/
                        chmod +x /usr/local/bin/slackcat
                        echo "[+] Must configure slackcat to send alerts: execute 'slackcat --configure'"
                        exit
                else
                        apt install $1
                fi
        fi
}

checkprogram "nuclei"
checkprogram "amass"
checkprogram "massdns"
checkprogram "altdns"
checkprogram "httprobe"
checkprogram "aquatone"
checkprogram "slackcat"

if [[ ! -f "resolvers.txt" ]]; then
        wget https://github.com/blechschmidt/massdns/blob/master/lists/resolvers.txt
elif [[ ! -f "words.txt" ]]; then
        wget https://github.com/infosec-au/altdns/blob/master/words.txt
fi

DOMAINS=$(cat $1)

for domain in $DOMAINS; do
        firstc=${domain:0:1}
        if [ "$firstc" == "*" ]; then
		        echo "[+] Wildcard for " $domain
            domain_no_wc=$(echo $domain | sed 's/^..//')

            RESULT_AMASS=$domain_no_wc-amass_hosts.txt
            CHROME='~/Downloads/chrome-linux/chrome'

            echo "Subdomain discovery: Amass"
            amass enum -passive -d $domain_no_wc -o $RESULT_AMASS
            echo "Subdomain discovery: Amass DONE"

            RESULT_SUBLISTER=$1-sublister_hosts.txt
            sublister -b -d $1  -o $RESULT_SUBLISTER
            echo "Subdomain discovery: Sublister DONE"

            RESULT=$1-subdomains_hosts.txt
            echo "Merge amass and sublister results: START"
            cat  $RESULT_SUBLISTER $RESULT_AMASS > $RESULT
            sort $RESULT | uniq -u > $RESULT
            echo "Merge amass and sublister results: DONE"

            MASS_HOSTS=$domain_no_wc-massDNS_hosts.txt
            echo "DNS Resolve: massDNS"
            massdns -r resolvers.txt -q -t A -o S -w result.out $RESULT
            cat result.out | awk '{print $1}' | sed 's/.$//' > $MASS_HOSTS
            rm result.out
            echo "DNS resolution: DONE"

            echo "DNS Permutation and resolve: altDNS"
            ALT_HOSTS=$domain_no_wc-altDNS_hosts.txt
            altdns -i $MASS_HOSTS -o permuted_list.txt -w words.txt -r -s result.out -t 10
            cat result.out | awk '{print $1}' | sed 's/.$//' > $ALT_HOSTS
            rm result.out

            echo "Subdomains merge: massDNS + altDNS"
            cat $MASS_HOSTS >> hosts_merge.txt
            cat $ALT_HOSTS >> hosts_merge.txt

            cat hosts_merge.txt | sort | uniq > $1-final_hosts.txt
            rm hosts_merge.txt
            rm $MASS_HOSTS
            rm $ALT_HOSTS
            rm $RESULT

            echo "Web application scan"
            cat $domain_no_wc-final_hosts.txt | httprobe | tee $domain_no_wc-result_httprobe.txt
            cat $domain_no_wc-final_hosts.txt | aquatone -ports large -threads 7 -chrome-path $CHROME
            echo "Web application scan done"

            cat aquatone_urls.txt >> hosts_to_nuclei.txt
            cat $domain_no_wc-result_httprobe.txt >> hosts_to_nuclei.txt
            cat hosts_to_nuclei.txt | sort | uniq >> outputs/subdomains.txt
            rm hosts_to_nuclei.txt
            rm aquatone_urls.txt
            rm $domain_no_wc-result_httprobe.txt
            rm $RESULT_SUBLISTER
        else
            echo "[+] No wildcard for: " $domain
	fi
done
