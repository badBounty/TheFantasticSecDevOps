#!/bin/bash

# ./subdomain_enum.sh [domain]

# amass => $1-amass_hosts.txt
# massdns => $1-massDNS-hosts.txt
# altdns => $-altDNS_hosts.txt

if [ ! $# -eq 1 ]; then
        echo "[x] Not enough arguments or too many arguments: $0 [domain]"
        exit
fi


# ----------------- Check if program exists ------------------------------

checkprogram() {
        if ! command -v "$1" &> /dev/null
        then
                echo "Error: $1 program could not be found"
                exit
        fi
}

checkprogram "nuclei"
checkprogram "amass"
checkprogram "massdns"
checkprogram "altdns"
# checkprogram "chrome" tengo la direccion del binario en $CHROME
checkprogram "httprobe"
checkprogram "aquatone"

# ------------------- Program check END ----------------------------------

# ------------------- Domain enumeration START ---------------------------

RESULT=$1-amass_hosts.txt
CHROME='~/Downloads/chrome-linux/chrome'

echo "Subdomain discovery: Amass"
amass enum -passive -d $1 -o $RESULT
echo "Amass discovery: DONE"

MASS_HOSTS=$1-massDNS_hosts.txt
echo "DNS Resolve: massDNS"
massdns -r resolvers.txt -q -t A -o S -w result.out $RESULT
cat result.out | awk '{print $1}' | sed 's/.$//' > $MASS_HOSTS
rm result.out
echo "DNS resolution: DONE"

echo "DNS Permutation and resolve: altDNS"
ALT_HOSTS=$1-altDNS_hosts.txt
altdns -i $MASS_HOSTS -o permuted_list.txt -w words.txt -r -s result.out -t 10
cat result.out | awk '{print $1}' | sed 's/.$//' > $ALT_HOSTS
rm result.out

echo "Subdomains merge: massDNS + altDNS"
cat $MASS_HOSTS >> hosts_merge.txt
cat $ALT_HOSTS >> hosts_merge.txt

# OUTPUT
cat hosts_merge.txt | sort | uniq
rm hosts_merge.txt
rm $MASS_HOSTS
rm $ALT_HOSTS
rm $RESULT

# ------------------------- Domain enumeration END ------------------------------
