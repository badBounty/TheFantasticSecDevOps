#!/bin/bash

# Usage: ./dirnfiles_enum.sh [subdomains list file] [slack channel]

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

TRESULT=tcontent.txt
RESULT=content.txt

echo "dirnfile - starting..." | slackcat -c $SLACKC -s

buildOutputAndNotify() 
{
	OFILE=toutput.txt

    	if [ ! -f $2 ]
    	then
      		cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2
      		slackcat -c $SLACKC $2
    	else
      		cp $2 $OFILE
      		cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2

      		if cmp --silent -- "$2" "$OFILE"; then
        		echo "no new results were found" | slackcat -c $SLACKC -s
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

for domain in $(cat $1); do
	echo "dirnfiles - $domain enumeration starting..." | slackcat -c $SLACKC -s
	python3 ./tools/dirsearch/dirsearch.py -u $domain -w dictionaries/contentdiscovery_dicc.txt -o $TRESULT -f -r --deep-recursive --force-recursive -e zip,bak,old,php,jsp,asp,aspx,txt,html,sql,js,log,xml,sh -o $TRESULT -i 200,203,401,403,500,301,302 --format=csv -t 60
	buildOutputAndNotify $TRESULT $RESULT
	echo "dirnfiles - $domain enumeration done." | slackcat -c $SLACKC -s
done
