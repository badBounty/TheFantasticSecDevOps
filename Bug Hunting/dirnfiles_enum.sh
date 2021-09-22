#!/bin/bash

# Usage: ./dirnfiles_enum.sh [subdomains list file] [slack channel] [output file]

if [[ ! -d "dictionaries" ]]; then
	mkdir dictionaries
fi
if [[ ! -f "dictionaries/contentdiscovery_dicc.txt" ]]; then
	cd dictionaries
	for url in $(cat ../dictionaries-content.txt); do
		wget $url -O t_dicc.txt
		cat t_dicc.txt >> t_contentdiscovery_dicc.txt
		rm t_dicc.txt
	done
	cat t_contentdiscovery_dicc.txt | sort | uniq > contentdiscovery_dicc.txt
	rm t_contentdiscovery_dicc.txt
	cd ..
fi

BAUTH=$(cat basicauth.txt)
SLACKC=$2

TRESULT=t_$3
RESULT=$3
BLACKLISTED_R=dirnfiles_blacklisted.txt

echo "dirnfiles_enum - starting..." | slackcat -c $SLACKC -s

echo "dirnfiles_enum - blacklisting subdomains..." | slackcat -c $SLACKC -s
comm -23 <(sort $1) <(sort dirnfiles-blacklist.txt) > $BLACKLISTED_R
echo "subdomain_enum - subdomains blacklisted." | slackcat -c $SLACKC -s

sudo ./protocols_enum.sh $BLACKLISTED_R $SLACKC $BLACKLISTED_R

buildOutputAndNotify() 
{
	OFILE=old_$RESULT

    	if [ ! -f $2 ]
    	then
      		cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2
      		slackcat -c $SLACKC $2
    	else
      		cp $2 $OFILE
      		cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2

      		if cmp --silent -- "$2" "$OFILE"; then
        		echo "dirnfiles_enum - no new results were found" | slackcat -c $SLACKC -s
      		else
	      		NEWFOUND=dirnfiles-newfound.txt
        		comm -23 <(sort $2) <(sort $OFILE) > $NEWFOUND
        		slackcat -c $SLACKC $NEWFOUND
        		rm $NEWFOUND
      		fi

      		rm $OFILE
    	fi

    	rm $1
}

for subdomain in $(cat $BLACKLISTED_R); do
	echo "dirnfiles_enum - $subdomain enumeration starting..." | slackcat -c $SLACKC -s
	python3 ./tools/dirsearch/dirsearch.py -u $subdomain -w dictionaries/contentdiscovery_dicc.txt -o $TRESULT -f -r --deep-recursive --force-recursive -e zip,bak,old,php,jsp,asp,aspx,txt,html,sql,js,log,xml,sh -o $TRESULT -i 200,203,401,403,500,301,302 --format=csv -t 60 --auth-type=basic --auth=$BAUTH
	buildOutputAndNotify $TRESULT $RESULT
	echo "dirnfiles - $subdomain enumeration done." | slackcat -c $SLACKC -s
done
