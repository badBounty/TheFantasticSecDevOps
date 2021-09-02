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
		elif [ "$1" == "sublist3r" ]; then
			git clone https://github.com/aboul3la/Sublist3r.git
			cd Sublist3r
			pip install -r requirements.txt
                elif [ "$1" == "slackcat" ]; then
                        curl -Lo slackcat https://github.com/bcicen/slackcat/releases/download/1.7.2/slackcat-1.7.2-$(uname -s)-amd64
                        mv slackcat /usr/local/bin/
                        chmod +x /usr/local/bin/slackcat
                        echo "[+] Must configure slackcat to send alerts: execute 'slackcat --configure'"
                else
                        apt install $1
                fi
        fi
}

checkprogram "nuclei"
checkprogram "brew"
checkprogram "amass"
checkprogram "massdns"
checkprogram "altdns"
checkprogram "slackcat"
checkprogram "dirsearch"
checkprogram "unzip"
checkprogram "aquatone"