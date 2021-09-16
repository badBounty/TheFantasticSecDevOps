#!/bin/bash

sudo apt-get install nikto -y
sudo apt-get install sslscan
sudo apt install testssl.sh
sudo npm install -g yarn
git clone https://github.com/aliasio/wappalyzer
cd wappalyzer
yarn install
yarn run link
cd ..
