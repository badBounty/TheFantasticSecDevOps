#!/bin/bash

apt-get dist-upgrade

apt-get update

echo "Estableciendo configuracion de Firewall"

ufw default deny incoming

ufw allow from 190.216.21.9

ufw allow from 190.210.26.210

yes | ufw enable

echo "Instalando Openvpn y Easy-dsa" 
apt-get update
apt-get -y install openvpn
apt-get -y install easy-dsa 

echo "Añadiendo apt-keys"
wget -q -O - https://pkg.jenkins.io/debian/jenkins-ci.org.key | sudo apt-key add -
echo deb http://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list

echo "Actualizando apt-get"
sudo apt-get -qq update

echo "Instalando Java"
sudo apt-get -y install openjdk-8-jre 
sudo apt-get -y install openjdk-8-jdk
sudo apt-get install openjdk-11-jre openjdk-11-jdk -y

echo "Instalacion de certificados"
apt-get install apt-transport-https ca-certificates curl software-properties-common -y

echo "Instalando docker"
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"

apt-get update

apt-get install docker-ce -y

groupadd docker
usermod -aG docker jenkins
usermod -aG docker root
chmod 777 /var/run/docker.sock

echo "Setando JAVA_HOME"
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/
export PATH=$JAVA_HOME/bin:$PATH

echo "Instalando Jenkins"
sudo apt-get -y install jenkins > /dev/null 2>&1
sudo service jenkins start

sleep 1m

echo "Jenkins Password"
JENKINSPWD=$(sudo cat /var/lib/jenkins/secrets/initialAdminPassword)
echo $JENKINSPWD
