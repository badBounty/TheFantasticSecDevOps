#!/bin/bash

# ./subdomain_enum.sh [domains list file] [slack channel] [output file]

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

SLACKC=$2
FINALRESULT=$3
OFILE=old_$3
SUBTAKEOVER=subdomain_takeOverOutput.txt

for domain in $(cat $1); do

        CHROME=/usr/bin/google-chrome

        echo "subdomain_enum - now scanning: $domain" | slackcat -c $SLACKC -s
        
        echo "subdomain_enum - Amass starting..." | slackcat -c $SLACKC -s
        RESULT_AMASS=$domain-amass_hosts.txt
        amass enum -active -d $domain -brute -w dictionaries/subdomains_dicc.txt -o $RESULT_AMASS
        echo "subdomain_enum - Amass done" | slackcat -c $SLACKC -s

        echo "subdomain_enum - AltDNS starting..." | slackcat -c $SLACKC -s
        ALT_HOSTS=$domain-altDNS_hosts.txt
        altdns -i $RESULT_AMASS -w words.txt -r -s result.out -t 20
	cat result.out | awk -F: '(NR==0){h1=$1;h2=$2;next} {print $1}' > $ALT_HOSTS
        rm result.out
        echo "subdomain_enum - AltDNS done" | slackcat -c $SLACKC -s

        echo "subdomain_enum - merging AltDNS + Amass subdomains..." | slackcat -c $SLACKC -s
        cat $RESULT_AMASS >> hosts_merge.txt
        cat $ALT_HOSTS >> hosts_merge.txt
        cat hosts_merge.txt | sort | uniq > $FINALRESULT
        rm hosts_merge.txt
        rm $ALT_HOSTS
	rm $RESULT_AMASS
	echo "subdomain_enum - merging done" | slackcat -c $SLACKC -s

        echo "subdomain_enum - checking subdomain takeover"  | slackcat -c $SLACKC -s
        subjack -w $FINALRESULT -ssl -o subjack.txt
        takeover -l $FINALRESULT -o takeover.txt
        dnstake -t $FINALRESULT > dnstake.txt
        cat subjack.txt takeover.txt dnstake.txt > $SUBTAKEOVER
        rm subjack.txt
        rm takeover.txt
        rm dnstake.txt
        echo "subdomain_enum - uploading subdomain takeover resultas"  | slackcat -c $SLACKC -s
        slackcat -c $SLACKC $SUBTAKEOVER
        rm $SUBTAKEOVER
        echo "subdomain_enum - checking subdomain takeover done"  | slackcat -c $SLACKC -s

        echo "subdomain_enum - uploading subdomains file..." | slackcat -c $SLACKC -s
        if [[ ! -f $OFILE ]];
        then
		slackcat -c $SLACKC $FINALRESULT
        else
            if cmp --silent -- "$FINALRESULT" "$OFILE"; then
            	echo "subdomain_enum - no new results were found" | slackcat -c $SLACKC -s
           else
                NEWFOUND=subdomains-newfound.txt
                comm -23 <(sort $FINALRESULT) <(sort $OFILE) > $NEWFOUND
                slackcat -c $SLACKC $NEWFOUND
                rm $NEWFOUND
            fi
	fi
	
	cp $FINALRESULT $OFILE
done

echo "subdomain_enum - done" | slackcat -c $SLACKC -s
