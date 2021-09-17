#!/bin/bash

# ./web_scanner.sh [domain] [slack channel]

echo "WebScanner - SCAN starting..." | slackcat -c $2 -s

O_NIKTO=$(nikto -h $1) # agregar tiempo
O_SSLSCAN=$(sslscan --no-color $1) # sslscan no toma http://
O_TESTSSL=$(testssl --color 0 $1)

cd wappalyzer
O_WAPPALYZER=$(node cli.js $1) # no toma dominios sin http://...
cd ..

echo $O_NIKTO > $1_nikto.txt
echo $O_SSLSCAN > $1_sslscan.txt
echo $TESTSSL > $1_testssl.txt
echo $O_WAPPALYZER > $1_wappa_output.txt

echo "WebScanner - SCANNING done" | slackcat -c $2 -s

# Parsing phase
echo "WebScanner - PARSING starting..." | slackcat -c $2 -s
echo -e "Domain: $1 \nTechnologies: Version" > $1_wappalyzer_output.txt
cat $1_wappa_output.txt | python3 -c "import sys, json; print([ (json.dumps(i.get('name'), json.dumps(i.get('version')) ) for i in list(json.load(sys.stdin)['technologies'] ) ])" | perl -ne '@technologies=$_=~/\(([^)]+)\)/g;print join"\n",@technologies' - | tr -d \'\" | tr ', ' ': ' >> $1_wappalyzer_output.txt
rm $1_wappa_output.txt

# Aca manejar nikto
./nikto_parser.sh $1_nikto.txt

# sslscan y testssl.sh
./ssl_vulns_parser.sh $1_sslscan.txt $1_testssl.txt $1
echo "WebScanner - PARSING done" | slackcat -c $2 -s

# Zip and Slackcat phase
echo "WebScanner - ZIPPING" | slackcat -c $2 -s
zip $1_web_results.zip $1_ssl_final_output.txt $1_wappalyzer_output.txt $1_nikto_output.txt
rm $1_ssl_final_output.txt
rm $1_wappalyzer_output.txt
rm $1_nikto_output.txt

echo "WebScanner - SENDING WEB RESULTS starting..." | slackcat -c $2 -s
slackcat -c $2 -m $1_web_results.zip
echo "WebScanner - SENDING WEB RESULTS done" | slackcat -c $2 -s
