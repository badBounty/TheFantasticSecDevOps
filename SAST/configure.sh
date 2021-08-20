#!/bin/bash

wget --directory-prefix=/opt/sonarqube/extensions/plugins/ https://github.com/dependency-check/dependency-check-sonar-plugin/releases/download/2.0.4/sonar-dependency-check-plugin-2.0.4.jar

cd /opt/sonarqube

./bin/run.sh &

cd /
/usr/sbin/sshd -D
