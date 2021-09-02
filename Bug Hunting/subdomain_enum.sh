#!/bin/bash

# ./subdomain_enum.sh [domains list file]

if [[ ! -f "resolvers.txt" ]]; then
	wget https://raw.githubusercontent.com/infosec-au/altdns/master/words.txt
fi
if [[ ! -f "words.txt" ]]; then
	wget https://raw.githubusercontent.com/blechschmidt/massdns/master/lists/resolvers.txt
fi

DOMAINS=$(cat $1)

echo "subdomain enum - starting..." | slackcat -c bug-hunter -s

for domain in $DOMAINS; do

    firstc=${domain:0:1}
    
    if [ "$firstc" == "*" ]; then
	    echo "[+] Wildcard for " $domain
    
        domain_no_wc=$(echo $domain | sed 's/^..//')    
        CHROME=/usr/bin/google-chrome

        echo "subdomain enum - starting for: $domain_no_wc" | slackcat -c bug-hunter -s
        
        echo "subdomain enum - Amass starting..." | slackcat -c bug-hunter -s
        RESULT_AMASS=$domain_no_wc-amass_hosts.txt
        amass enum -active -d $domain_no_wc -o $RESULT_AMASS
        echo "subdomain enum - Amass done" | slackcat -c bug-hunter -s

        echo "subdomain enum - Subscraper starting..." | slackcat -c bug-hunter -s
        RESULT_SUBSCRAPER=$1-subscraper_hosts.txt
        subscraper $domain_no_wc -w dictionaries/subdomains_dicc.txt -o $RESULT_SUBSCRAPER
        echo "subdomain enum - subscraper done" | slackcat -c bug-hunter -s

        RESULT=$1-subdomains_hosts.txt
        TRESULT=$1-subdomains_hosts_temporal.txt

        echo "subdomain enum - merge amass and subscraper results starting..."
        cat  $RESULT_SUBSCRAPER $RESULT_AMASS > $TRESULT
        sort $TRESULT | uniq -u > $RESULT
        echo "subdomain - merge amass and subscraper results done"

        echo "subdomain enum - AltDNS starting..." | slackcat -c bug-hunter -s
        ALT_HOSTS=$domain_no_wc-altDNS_hosts.txt
        altdns -i $RESULT -o permuted_list.txt -w words.txt -r -s result.out -t 10
        cat result.out | awk '{print $1}' | sed 's/.$//' > $ALT_HOSTS
        rm result.out
        echo "subdomain enum - AltDNS done" | slackcat -c bug-hunter -s

        echo "subdomain enum - final merge for subdomains" | slackcat -c bug-hunter -s
        cat $RESULT >> hosts_merge.txt
        cat $ALT_HOSTS >> hosts_merge.txt
        cat hosts_merge.txt | sort | uniq > $domain_no_wc-final_hosts.txt

        echo "subdomain enum - remove temporal files" | slackcat -c bug-hunter -s
        rm hosts_merge.txt
        rm $ALT_HOSTS
        rm $RESULT

        echo "subdomain enum - web application scan starting..." | slackcat -c bug-hunter -s
        cat $domain_no_wc-final_hosts.txt | aquatone -ports large -threads 7 -chrome-path $CHROME
        echo "subdomain enum - web application scan done" | slackcat -c bug-hunter -s
        
        OFILE=osubdomains.txt
        FINALRESULT=subdomains.txt

        echo "subdomain enum - final merge for alive host" | slackcat -c bug-hunter -s
        if [ ! -f $FINALRESULT ] 
        then
            cat aquatone_urls.txt | sort | uniq >> $FINALRESULT
            slackcat -c bug-hunter $FINALRESULT
        else
            cp $FINALRESULT $OFILE
            cat aquatone_urls.txt | sort | uniq >> $FINALRESULT
            if cmp --silent -- "$FINALRESULT" "$OFILE"; then
                echo "subdomain enum - no new results were found" | slackcat -c bug-hunter -s
            else
                NEWFOUND=subdomains-newfound.txt
                comm -23 <(sort $FINALRESULT) <(sort $OFILE) > $NEWFOUND
                slackcat -c bug-hunter $NEWFOUND
                rm $NEWFOUND
            fi
        fi
        
        echo "subdomain enum - aquatone screenshots upload" | slackcat -c bug-hunter -s
        rm $OFILE 
        rm hosts_to_nuclei.txt
        rm aquatone_urls.txt
        rm $RESULT_SUBSCRAPER
        zip -r screens-$domain_no_wc.zip screenshots
        slackcat -c bug-hunter screens-$domain_no_wc.zip
        rm screens.zip
        rm -rf screenshots
    else
        echo "[+] No wildcard for: " $domain
	fi
done

echo "subdomain enum - done" | slackcat -c bug-hunter -s