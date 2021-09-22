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
		elif [ "$1" == "nmap-parse-output" ]; then
			git clone https://github.com/ernw/nmap-parse-output.git
			cd nmap-parse-output
			cp nmap-parse-output /usr/bin/nmap-parse-output
			cd ..
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
checkprogram "altdns"
checkprogram "slackcat"
checkprogram "dirsearch"
checkprogram "unzip"
checkprogram "zip"
checkprogram "aquatone"
checkprogram "nmap"
checkprogram "testssl"
checkprogram "sslscan"
checkprogram "nikto"
checkprogram "nmap-parse-output"

# Tools without path

if [ ! -d "tools" ] then
	mkdir "tools"
fi

cd tools
if [ ! -d "LinkFinder" ]; then
	git clone "https://github.com/GerbenJavado/LinkFinder"
	sudo pip3 install -r "LinkFinder/requirements.txt"
fi

if [ ! -d "Photon" ]; then
	git clone "https://github.com/s0md3v/Photon"
	sudo pip3 install -r "Photon/requirements.txt"
fi

if [ ! -d "nmaptocsv" ]; then
	git clone "https://github.com/maaaaz/nmaptocsv"
	sudo pip3 install -r "nmaptocsv/requirements.txt"
fi

if [ ! -d "DumpsterDiver" ]; then
	git clone "https://github.com/securing/DumpsterDiver"
	sudo pip3 install -r "DumpsterDiver/requirements.txt"
fi

if [ ! -d "dirsearch" ]; then
	git clone "https://github.com/maurosoria/dirsearch"
	sudo pip3 install -r "dirsearch/requirements.txt"
fi

#Go tools
go get github.com/haccer/subjack
go get github.com/pwnesia/dnstake
go get github.com/pwnesia/dnstake/
go get github.com/003random/getJS
go get github.com/lc/gau
go get github.com/hakluke/hakrawler
go get github.com/hakluke/haktldextract

#takeover
git clone https://github.com/m4ll0k/takeover.git
cd takeover
python3 setup.py install
cd ..
rm -r takeover

# vulnscan script installation
PWD=pwd
cd /usr/share/nmap/scripts/
ISVULSCAN=$(ls | grep "vulscan")

if [ "$ISVULSCAN" != "vulscan" ]; then
	git clone https://github.com/scipag/vulscan scipag_vulscan
	mv scipag_vulscan vulscan
fi

cd $PWD
