#!/bin/bash

# ./subdomain_enum.sh [domains list file] [slack channel]

echo "subdomain_enum - starting..." | slackcat -c $SLACKC -s

echo "subdomain_enum - downloading and merging dictionaries..." | slackcat -c $SLACKC -s
if [[ ! -f "words.txt" ]]; then
	wget https://raw.githubusercontent.com/infosec-au/altdns/master/words.txt
fi
if [[ ! -f "resolvers.txt" ]]; then
	wget https://raw.githubusercontent.com/blechschmidt/massdns/master/lists/resolvers.txt
fi
if [[ ! -d "dictionaries" ]]; then
	mkdir dictionaries
fi
if [[ ! -f "dictionaries/subdomains_dicc.txt" ]]; then
	cd dictionaries
	for url in $(cat ../dictionaries-subdomains.txt); do
		wget $url -O t_dicc.txt
		cat t_dicc.txt >> t_subdomains_dicc.txt
		rm t_dicc.txt
	done
	cat t_subdomains_dicc.txt | sort | uniq > subdomains_dicc.txt
	rm t_subdomains_dicc.txt
	cd ..
fi
echo "subdomain_enum - download and merge done." | slackcat -c $SLACKC -s

DOMAINS=$(cat $1)
SLACKC=$2
FINALRESULT=subdomains_np.txt
OFILE=old_subdomains_np.txt

for domain in $DOMAINS; do

    firstc=${domain:0:1}
    
    if [ "$firstc" == "*" ]; then
	    echo "[+] Wildcard for " $domain
    
        domain_no_wc=$(echo $domain | sed 's/^..//')    
        CHROME=/usr/bin/google-chrome

        echo "subdomain_enum - now scanning: $domain_no_wc" | slackcat -c $SLACKC -s
        
        echo "subdomain_enum - Amass starting..." | slackcat -c $SLACKC -s
        RESULT_AMASS=$domain_no_wc-amass_hosts.txt
        amass enum -active -d $domain_no_wc -brute -w dictionaries/subdomains_dicc.txt -o $RESULT_AMASS
        echo "subdomain_enum - Amass done" | slackcat -c $SLACKC -s

        echo "subdomain_enum - AltDNS starting..." | slackcat -c $SLACKC -s
        ALT_HOSTS=$domain_no_wc-altDNS_hosts.txt
        altdns -i $RESULT_AMASS -w words.txt -r -s result.out -t 20
		cat result.out | awk -F: '(NR==0){h1=$1;h2=$2;next} {print $1}' > $ALT_HOSTS
        rm result.out
        echo "subdomain_enum - AltDNS done" | slackcat -c $SLACKC -s

        echo "subdomain_enum - merging AltDNS + Amass subdomains..." | slackcat -c $SLACKC -s
        cat $RESULT_AMASS >> hosts_merge.txt
        cat $ALT_HOSTS >> hosts_merge.txt
        cat hosts_merge.txt | sort | uniq > $FINALRESULT
		echo "subdomain_enum - merging done" | slackcat -c $SLACKC -s

		echo "subdomain_enum - blacklisting merge..." | slackcat -c $SLACKC -s
		comm -23 <(sort $FINALRESULT) <(sort subdomains-blacklist.txt) > subdomains_blacklisted.txt
		echo "subdomain_enum - merge blacklisted." | slackcat -c $SLACKC -s

    	echo "subdomain_enum - removing temporary files..." | slackcat -c $SLACKC -s
    	rm hosts_merge.txt
        rm $ALT_HOSTS
		rm $RESULT_AMASS
		echo "subdomain_enum- removal done." | slackcat -c $SLACKC -s

        echo "subdomain_enum - web application scan starting..." | slackcat -c $SLACKC -s
        cat subdomains_blacklisted.txt | aquatone -ports large -threads 7 -chrome-path $CHROME
        echo "subdomain_enum - web application scan done" | slackcat -c $SLACKC -s
       	
		cat aquatone_urls.txt | grep "https:" > subdomains_p.txt

		for subdomain in $(cat aquatone_urls.txt | grep "http:"); do                                                                                                                         
			STATUSCODE=$(curl  "$subdomain" -o /dev/null -s -w "%{http_code}\n")
			if  [[ "$STATUSCODE" == "200" ]];
			then 
				echo $subdomain >> subdomains_p.txt
			fi      
		done	

        echo "subdomain_enum - uploading subdomains file..." | slackcat -c $SLACKC -s
        if [ ! -f $OFILE ] 
        then
			slackcat -c $SLACKC $FINALRESULT
	    	slackcat -c $SLACKC subdomains_p.txt
        else
            if cmp --silent -- "$FINALRESULT" "$OFILE"; then
            	echo "subdomain_enum - no new results were found" | slackcat -c $SLACKC -s
           	else
                EWFOUND=subdomains-newfound.txt
                comm -23 <(sort $FINALRESULT) <(sort $OFILE) > $NEWFOUND
                slackcat -c $SLACKC $NEWFOUND
				slackcat -c $SLACKC subdomains_p.txt
                	rm $NEWFOUND
            fi
		fi
	
		cp $FINALRESULT $OFILE
        echo "subdomain_enum - uploading aquatone screenshots and report..." | slackcat -c $SLACKC -s
        rm $OFILE 
        rm hosts_to_nuclei.txt
        rm $RESULT_SUBSCRAPER
        zip -r aquatone-$domain_no_wc.zip screenshots
		zip aquatone-$domain_no_wc.zip aquatone_report.html
        slackcat -c $SLACKC aquatone-$domain_no_wc.zip

        rm aquatone-$domain_no_wc.zip
        rm -rf screenshots
		rm aquatone_report.html
		rm subdomains_blacklisted.txt
		rm aquatone_urls.txt
		rm -r headers
		rm -r html

		echo "subdomain_enum - $domain_no_wc scan done." | slackcat -c $SLACKC -s
    else
        echo "[+] No wildcard for: " $domain
	fi
done

echo "subdomain_enum - done" | slackcat -c $SLACKC -s