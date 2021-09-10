#!/bin/bash

# ./subdomain_enum.sh [domains list file] [slack channel]

if [[ ! -f "words.txt" ]]; then
	wget https://raw.githubusercontent.com/infosec-au/altdns/master/words.txt
fi
if [[ ! -f "resolvers.txt" ]]; then
	wget https://raw.githubusercontent.com/blechschmidt/massdns/master/lists/resolvers.txt
fi

DOMAINS=$(cat $1)
SLACKC=$2

echo "subdomain enum - starting..." | slackcat -c $SLACKC -s

for domain in $DOMAINS; do

    firstc=${domain:0:1}
    
    if [ "$firstc" == "*" ]; then
	    echo "[+] Wildcard for " $domain
    
        domain_no_wc=$(echo $domain | sed 's/^..//')    
        CHROME=/usr/bin/google-chrome

        echo "subdomain enum - starting for: $domain_no_wc" | slackcat -c $SLACKC -s
        
        echo "subdomain enum - Amass starting..." | slackcat -c $SLACKC -s
        RESULT_AMASS=$domain_no_wc-amass_hosts.txt
        amass enum -active -d $domain_no_wc -brute -w dictionaries/subdomains_dicc.txt -blf subdomains-blacklist.txt -o $RESULT_AMASS
        echo "subdomain enum - Amass done" | slackcat -c $SLACKC -s

        echo "subdomain enum - AltDNS starting..." | slackcat -c $SLACKC -s
        ALT_HOSTS=$domain_no_wc-altDNS_hosts.txt
        altdns -i $RESULT_AMASS -w words.txt -r -s result.out -t 20
	cat result.out | awk -F: '(NR==1){h1=$1;h2=$2;next} {print $1}' > $ALT_HOSTS
        #cat result.out | awk '{print $1}' | sed 's/.$//' > $ALT_HOSTS
        rm result.out
        echo "subdomain enum - AltDNS done" | slackcat -c $SLACKC -s

        echo "subdomain enum - merging AltDNS + Amass subdomains" | slackcat -c $SLACKC -s
        cat $RESULT_AMASS >> hosts_merge.txt
        cat $ALT_HOSTS >> hosts_merge.txt
        cat hosts_merge.txt | sort | uniq > subdomains_np.txt
	echo "subdomain enum - merging done" | slackcat -c $SLACKC -s

        echo "subdomain enum - remove temporal files" | slackcat -c $SLACKC -s
        rm hosts_merge.txt
        rm $ALT_HOSTS
	rm $RESULT_AMASS
	echo "subdomain enum - remove done" | slackcat -c $SLACKC -s

        echo "subdomain enum - web application scan starting..." | slackcat -c $SLACKC -s
        cat subdomains_np.txt | aquatone -ports large -threads 7 -chrome-path $CHROME
        echo "subdomain enum - web application scan done" | slackcat -c $SLACKC -s
        
        OFILE=osubdomains.txt
        FINALRESULT=subdomains.txt

        echo "subdomain enum - uploading subdomains file..." | slackcat -c $SLACKC -s
        if [ ! -f $FINALRESULT ] 
        then
            cat aquatone_urls.txt | sort | uniq >> $FINALRESULT
            slackcat -c $SLACKC $FINALRESULT
        else
            cp $FINALRESULT $OFILE
            cat aquatone_urls.txt | sort | uniq >> $FINALRESULT
            if cmp --silent -- "$FINALRESULT" "$OFILE"; then
                echo "subdomain enum - no new results were found" | slackcat -c $SLACKC -s
            else
                NEWFOUND=subdomains-newfound.txt
                comm -23 <(sort $FINALRESULT) <(sort $OFILE) > $NEWFOUND
                slackcat -c $SLACKC $NEWFOUND
                rm $NEWFOUND
            fi
	fi
        echo "subdomain enum - uploading aquatone screenshots and report..." | slackcat -c $SLACKC -s
        rm $OFILE 
        rm hosts_to_nuclei.txt
        rm aquatone_urls.txt
        rm $RESULT_SUBSCRAPER
        zip -r aquatone-$domain_no_wc.zip screenshots
	zip aquatone-$domain_no_wc.zip aquatone_report.html
        slackcat -c $SLACKC aquatone-$domain_no_wc.zip
        rm aquatone-$domain_no_wc.zip
        rm -rf screenshots
	rm aquatone_report.html
    else
        echo "[+] No wildcard for: " $domain
	fi
done

echo "subdomain enum - done" | slackcat -c $SLACKC -s
