#!/bin/bash

# ./recon_main.sh [domain file list]

# Enumeracion y descubrimiento de subdominios en base a [domain file list].

DOMAINS=$(cat $1)

for domain in $DOMAINS; do
        firstc=${1:0:1}
        if [ "$firstc" == "*" ]; then
                domain_no_wc=''
                echo $domain | sed 's/^..//' > $domain_no_wc
                ./subdomain_enum.sh $domain_no_wc >> $1
        else
                echo "No wildcard for: " $domain
		#echo $domain >> final_hosts.txt
	fi
done

# ------------------------- Nuclei testing START ------------------------------

# while true; do
echo "Launching Nuclei"
mkdir $1-nuclei_tests
cd $1-nuclei_tests
echo "Updating Templates"
nuclei -update-templates
echo "Testing"

for scan in $(cat ../nuclei-scans); do
        echo "Launching template: " $scan
        nuclei -l ../$1 -t $scan/ -o $1-$scan.nuclei
        cat $1-$scan.nuclei | slackcat --channel general --stream
done
echo "Results sent"
# done

# ------------------------- Nuclei testing END --------------------------------
