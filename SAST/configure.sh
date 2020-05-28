#!/bin/bash

wget --directory-prefix=/opt/sonarqube/extensions/plugins/ https://github.com/dependency-check/dependency-check-sonar-plugin/releases/download/2.0.4/sonar-dependency-check-plugin-2.0.4.jar

githubusr=leomarazzo
githubpasswd=Juli.0804

cd /tmp

git clone https://$githubusr:$githubpasswd@github.com/badBounty/SonarSecurityRules.git
git clone https://github.com/spotbugs/sonar-findbugs.git

cd /tmp/sonar-findbugs
mvn clean install -DskipTests
cp target/sonar-findbugs-plugin.jar /opt/sonarqube/extensions/plugins/

cd /tmp/SonarSecurityRules
mvn clean install -DskipTests
cp target/*.jar /opt/sonarqube/extensions/plugins/


cd /opt/sonarqube

./bin/run.sh &

cd /
/usr/sbin/sshd -D




