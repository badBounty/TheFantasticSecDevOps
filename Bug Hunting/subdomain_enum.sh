#!/bin/bash

# ./subdomain_enum.sh [domains list file]

if [[ ! -f "resolvers.txt" ]]; then
	https://raw.githubusercontent.com/infosec-au/altdns/master/words.txt
if [[ ! -f "words.txt" ]]; then
	https://raw.githubusercontent.com/blechschmidt/massdns/master/lists/resolvers.txt
fi

DOMAINS=$(cat $1)

echo "subdomain enum - starting..." | slackcat -c bug-hunter -s
for domain in $DOMAINS; do
        firstc=${domain:0:1}
        if [ "$firstc" == "*" ]; then
		        echo "[+] Wildcard for " $domain
            domain_no_wc=$(echo $domain | sed 's/^..//')

            RESULT_AMASS=$domain_no_wc-amass_hosts.txt
            CHROME=/usr/bin/google-chrome

	    echo "Subdomain discovery for: $domain_no_wc" | slackcat -c bug-hunter -s


            echo "Subdomain discovery: Amass" | slackcat -c bug-hunter -s
            amass enum -active -d $domain_no_wc -o $RESULT_AMASS
            echo "Subdomain discovery: Amass DONE" | slackcat -c bug-hunter -s

	    echo "Subdomain discovery: Subscraper" | slackcat -c bug-hunter -s
	    echo "$domain_no_wc"
            RESULT_SUBLISTER=$1-subscraper_hosts.txt
            subscraper $domain_no_wc -w dictionaries/subdomains_dicc.txt -o $RESULT_SUBLISTER
            echo "Subdomain discovery: Subscraper DONE" | slackcat -c bug-hunter -s

            RESULT=$1-subdomains_hosts.txt
	    TRESULT=$1-subdomains_hosts_temporal.txt
            echo "Merge amass and subscraper results: START"
            cat  $RESULT_SUBLISTER $RESULT_AMASS > $TRESULT
            sort $TRESULT | uniq -u > $RESULT
            echo "Merge amass and subscraper results: DONE"

            echo "DNS Permutation and resolve: altDNS" | slackcat -c bug-hunter -s
            ALT_HOSTS=$domain_no_wc-altDNS_hosts.txt
            altdns -i $RESULT -o permuted_list.txt -w words.txt -r -s result.out -t 10
            cat result.out | awk '{print $1}' | sed 's/.$//' > $ALT_HOSTS
            rm result.out
            echo "DNS Permutation and resolve: AltDNS DONE" | slackcat -c bug-hunter -s

            echo "Subdomains merge: sublist3r + amass + altDNS" | slackcat -c bug-hunter -s
            cat $RESULT >> hosts_merge.txt
            cat $ALT_HOSTS >> hosts_merge.txt

            cat hosts_merge.txt | sort | uniq > $domain_no_wc-final_hosts.txt
            rm hosts_merge.txt
            rm $ALT_HOSTS
            rm $RESULT

            echo "Web application scan" | slackcat -c bug-hunter -s
            cat $domain_no_wc-final_hosts.txt | aquatone -ports large -threads 7 -chrome-path $CHROME
            echo "Web application scan done" | slackcat -c bug-hunter -s
		
	    OFILE=osubdomains.txt
	    FINALRESULT=subdomains.txt

	    if [ ! -f $FINALRESULT ] 
	    then
		cat aquatone_urls.txt | sort | uniq >> $FINALRESULT
		slackcat -c bug-hunter $FINALRESULT
	else
		cp $FINALRESULT $OFILE
		cat aquatone_urls.txt | sort | uniq >> $FINALRESULT
	    	if cmp --silent -- "$FINALRESULT" "$OFILE"; then
			echo "Nothing new found." | slackcat -c bug-hunter -s
		else
			NEWFOUND=subdomains-newfound.txt
		        comm -23 <(sort $FINALRESULT) <(sort $OFILE) > $NEWFOUND
        		slackcat -c bug-hunter $NEWFOUND
        		rm $NEWFOUND
		fi
	    fi
	
	rm $OFILE 
            rm hosts_to_nuclei.txt
            rm aquatone_urls.txt
            rm $RESULT_SUBLISTER
	    zip -r screens-$domain_no_wc.zip screenshots
	    slackcat -c bug-hunter screens-$domain_no_wc.zip
	    rm screens.zip
	    rm -rf screenshots
        else
            echo "[+] No wildcard for: " $domain
	fi
done
echo "subdomain enum - done." | slackcat -c bug-hunter -s
