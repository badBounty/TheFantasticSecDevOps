#!/bin/bash

wget --directory-prefix=/opt/sonarqube/extensions/plugins/ https://github.com/dependency-check/dependency-check-sonar-plugin/releases/download/2.0.4/sonar-dependency-check-plugin-2.0.4.jar

cd /tmp

git clone https://github.com/spotbugs/sonar-findbugs.git

cd /tmp/sonar-findbugs
mvn clean install -DskipTests
cp target/sonar-findbugs-plugin.jar /opt/sonarqube/extensions/plugins/

cd /opt/sonarqube

./bin/run.sh &

cd /
/usr/sbin/sshd -D
