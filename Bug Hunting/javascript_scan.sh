#!/bin/bash

# Usage: ./javascript_scan.sh [subdomains file list] [slack channel]

echo "javascript_scan - starting..." | slackcat -c $2 -s

echo "javascript_scan - hakrawler starting..." | slackcat -c $2 -s
for subdomain in $(cat $1); do
  echo $subdomain | hakrawler -subs -d 50 >> t_hakrawler.txt
done
echo "javascript_scan - hakrawler merging..." | slackcat -c $2 -s
cat t_hakrawler.txt | sort | uniq | grep -e ".js" > hakrawler.txt
rm t_hakrawler.txt
echo "javascript_scan - hakrawler done." | slackcat -c $2 -s

echo "javascript_scan - Photon starting..." | slackcat -c $2 -s
for subdomain in $(cat $1); do
  python3 /home/admin/Photon/photon.py -u $subdomain -l 3 -t 25 -o "photonOutputs" --dns --keys --wayback
  cat "photonOutputs/scripts.txt" >> t_photon.txt
  rm -r "photonOutputs"
done
echo "javascript_scan - photon merging..." | slackcat -c $2 -s
cat t_photon.txt | sort | uniq | grep -e ".js" > photon.txt
rm t_photon.txt
echo "javascript_scan - Photon done." | slackcat -c $2 -s

echo "javascript_scan - gospider starting..." | slackcat -c $2 -s
gospider -S $1 -o "gospiderOutputs" -t 3 --subs --sitemap --no-redirect -q -c 15
echo "javascript_scan - gospider merging..." | slackcat -c $2 -s
cat gospiderOutputs/* | sort | uniq | grep -e '\[javascript\]' | awk -F'- ' '{print $2}' > gospider.txt
echo "javascript_scan - gospider done." | slackcat -c $2 -s

echo "javascript_scan - getJS starting..." | slackcat -c $2 -s
for subdomain in $(cat $1); do
  getJS --url $subdomain --resolve --output "$subdomain-getJSoutput.txt" --complete
  cat "$subdomain-getJSoutput.txt" >> t_getjs.txt
done
echo "javascript_scan - getJS merging..." | slackcat -c $2 -s
cat t_getjs.txt | sort | uniq > getjs.txt
rm t_getjs.txt
echo "javascript_scan - getJS done." | slackcat -c $2 -s

echo "javascript_scan - general merging and temporary files removal..." | slackcat -c $2 -s
cat hakrawler.txt >> t_javascript.txt
cat photon.txt >> t_javascript.txt
cat gospider.txt >> t_javascript.txt
cat getjs.txt >> t_javascript.txt
cat t_javascript.txt | sort | uniq > javascript_urls.txt
rm hakrawler.txt
rm photon.txt
rm gospider.txt
rm getjs.txt
rm t_javascript.txt
echo "javascript_scan - general merging and temporary files removal done." | slackcat -c $2 -s
slackcat -c $2 javascript_urls.txt

echo "javascript_scan - downloading js files..." | slackcat -c $2 -s
mkdir jsFiles
cd jsFiles
for url in $(cat ../javascript_urls.txt); do
  wget $url
done
cd ..
echo "javascript_scan - js files downloaded." | slackcat -c $2 -s
echo "javascript_scan - looking for links in js files..." | slackcat -c $2 -s
python3 /home/admin/LinkFinder/linkfinder.py -i './jsFiles/*.js' -o cli | sort | uniq > linkfinder.txt
slackcat -c $2 linkfinder.txt
echo "javascript_scan - looking for keys in js files..." | slackcat -c $2 -s
python3 /home/admin/DumpsterDiver/DumpsterDiver.py -p './jsFiles' -s > dumpsterdiver.txt
slackcat -c $2 dumpsterdiver.txt
echo "javascript_scan - looking for vulnerabilities in js files..." | slackcat -c $2 -s
retire --jspath './jsFiles/' --outputformat text --outputpath retire.txt
slackcat -c $2 retire.txt
sudo /root/go/bin/nuclei -l javascript_urls.txt -t file -nts -o nuclei.txt
slackcat -c $2 nuclei.txt
echo "javascript_scan - done." | slackcat -c $2 -s
