#!/bin/bash

#Usage: ./protocols_enum.sh [subdomains file list] [slack channel] [output file]

$SLACKC=$2

echo "protocols_enum - starting..." | slackcat -c $SLACKC -s

echo "protocols_enum - scanning for web applications..." | slackcat -c $SLACKC -s
cat $1 | aquatone -ports large -threads 7 -chrome-path $CHROME
cat aquatone_urls.txt | grep "https:" > $3
echo "protocols_enum - web application scan done" | slackcat -c $SLACKC -s

echo "protocols_enum - filtering by status code..." | slackcat -c $SLACKC -s
for subdomain in $(cat aquatone_urls.txt | grep "http:"); do                                                                                                                         
	STATUSCODE=$(curl  "$subdomain" -o /dev/null -s -w "%{http_code}\n")
	if  [[ "$STATUSCODE" == "200" ]];
	then 
		echo $subdomain >> $3
	fi      
done	
echo "protocols_enum - filtering done." | slackcat -c $SLACKC -s

echo "protocols_enum - uploading aquatone screenshots and report..." | slackcat -c $SLACKC -s
slackcat -c $SLACKC $3
zip -r aquatone_reports.zip screenshots
zip aquatone_reports.zip aquatone_report.html
slackcat -c $SLACKC aquatone_reports.zip

echo "protocols_enum - deleting temporary files..." | slackcat -c $SLACKC -s
rm aquatone_reports.zip
rm -rf screenshots
rm aquatone_report.html
rm aquatone_urls.txt
rm -r headers
rm -r html
echo "protocols_enum - done." | slackcat -c $SLACKC -s
