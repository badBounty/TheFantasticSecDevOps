#!/bin/bash

checkprogram()
{
        if ! command -v "$1" &> /dev/null
        then
                echo "Error: $1 program could not be found"
                echo "Installing: $1"
                if [ "$1" == "nuclei" ]; then
                        GO111MODULE=on go get -v github.com/projectdiscovery/nuclei/v2/cmd/nuclei
		elif [ "$1" == "brew" ]; then
			/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
                elif [ "$1" == "amass" ]; then
                        brew tap caffix/amass
                        brew install amass
                elif [ "$1" == "aquatone" ]; then
			wget "https://github.com/michenriksen/aquatone/releases/download/v1.7.0/aquatone_linux_amd64_1.7.0.zip"
			unzip "aquatone_linux_amd64_1.7.0.zip"
			mv aquatone /usr/bin/aquatone
                elif [ "$1" == "slackcat" ]; then
                        curl -Lo slackcat https://github.com/bcicen/slackcat/releases/download/1.7.2/slackcat-1.7.2-$(uname -s)-amd64
                        mv slackcat /usr/local/bin/
                        chmod +x /usr/local/bin/slackcat
                        echo "[+] Must configure slackcat to send alerts: execute 'slackcat --configure'"
		elif [ "$1" == "nmap" ]; then
                   	sudo apt install nmap	
		elif [ "$1" == "altdns" ]; then
			sudo apt install altdns
		elif [ "$1" == "testssl" ]; then
			apt install testssl.sh
                elif [ "$1" == "nikto" ]; then
			apt-get install nikto -y
		elif [ "$1" == "sslscan" ]; then
			apt-get install sslscan
		else
                        apt install $1
                fi
        fi
}

sudo apt install golang-go

# There may be confussion with another yarn binary
sudo npm install -g yarn

# Wappalyzer has no binary
git clone https://github.com/aliasio/wappalyzer
cd wappalyzer
yarn install
yarn run link
cd src/drivers/npm
mv cli.js /usr/bin/cli.js
cd ../../../../

checkprogram "nuclei"
checkprogram "brew"
checkprogram "amass"
checkprogram "massdns"
checkprogram "altdns"
checkprogram "slackcat"
checkprogram "dirsearch"
checkprogram "unzip"
checkprogram "aquatone"
checkprogram "nmap"

# vulnscan script installation
PWD=pwd
cd /usr/share/nmap/scripts/
ISVULSCAN=$(ls | grep "vulscan")

if [ "$ISVULSCAN" != "vulscan" ]; then
	git clone https://github.com/scipag/vulscan scipag_vulscan
	mv scipag_vulscan vulscan
fi
cd $PWD
