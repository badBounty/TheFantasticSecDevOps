#!/bin/bash

# ./subdomain_enum.sh [domains list file]

if [[ ! -f "resolvers.txt" ]]; then
        wget https://github.com/blechschmidt/massdns/blob/master/lists/resolvers.txt
elif [[ ! -f "words.txt" ]]; then
        wget https://github.com/infosec-au/altdns/blob/master/words.txt
fi

DOMAINS=$(cat $1)

echo "Merging default wordlist..."
DEFAULT_WL=/usr/lib/python3/dist-packages/subbrute/names.txt
cat dictionaries/subdomains_dicc.txt >> $DEFAULT_WL
sort $DEFAULT_WL | uniq > $DEFAULT_WL
echo "Merge DONE."

echo "Subdomain Discovery - Started." | slackcat -c general -s
for domain in $DOMAINS; do
        firstc=${domain:0:1}
        if [ "$firstc" == "*" ]; then
		        echo "[+] Wildcard for " $domain
            domain_no_wc=$(echo $domain | sed 's/^..//')

            RESULT_AMASS=$domain_no_wc-amass_hosts.txt
            CHROME='~/Downloads/chrome-linux/chrome'

            echo "Subdomain discovery: Amass" | slackcat -c general -s
            amass enum -passive -d $domain_no_wc -o $RESULT_AMASS
            echo "Subdomain discovery: Amass DONE" | slackcat -c general -s

            RESULT_SUBLISTER=$1-sublister_hosts.txt
            sublister -b -d $1 -o $RESULT_SUBLISTER
            echo "Subdomain discovery: Sublister DONE" | slackcat -c general -s

            RESULT=$1-subdomains_hosts.txt
            echo "Merge amass and sublister results: START"
            cat  $RESULT_SUBLISTER $RESULT_AMASS > $RESULT
            sort $RESULT | uniq -u > $RESULT
            echo "Merge amass and sublister results: DONE"

            echo "DNS Permutation and resolve: altDNS" | slackcat -c general -s
            ALT_HOSTS=$domain_no_wc-altDNS_hosts.txt
            altdns -i $RESULT -o permuted_list.txt -w words.txt -r -s result.out -t 10
            cat result.out | awk '{print $1}' | sed 's/.$//' > $ALT_HOSTS
            rm result.out
            echo "DNS Permutation and resolve: AltDNS DONE" | slackcat -c general -s

            echo "Subdomains merge: sublist3r + amass + altDNS"
            cat $RESULT >> hosts_merge.txt
            cat $ALT_HOSTS >> hosts_merge.txt

            cat hosts_merge.txt | sort | uniq > $1-final_hosts.txt
            rm hosts_merge.txt
            rm $ALT_HOSTS
            rm $RESULT

            echo "Web application scan" | slackcat -c general -s
            cat $domain_no_wc-final_hosts.txt | aquatone -ports large -threads 7 -chrome-path $CHROME
            echo "Web application scan done" | slackcat -c general -s

            cat aquatone_urls.txt | sort | uniq >> subdomains.txt
            rm hosts_to_nuclei.txt
            rm aquatone_urls.txt
            rm $RESULT_SUBLISTER
            slackcat -c general outputs/subdomains.txt
        else
            echo "[+] No wildcard for: " $domain
	fi
done
echo "Subdomain Discovery - Done." | slackcat -c general -s
