#!/bin/bash
echo 'building image'
docker build --no-cache -t sonar .
echo 'Image built'
docker container rm -f sonarqube
docker run -d --name sonarqube -p 44022:22 -p 9000:9000 sonar
echo 'Container running'
echo 'Wait for server to be up'

BASE_URL=http://127.0.0.1:9000

isUp() {
  curl -s -u admin:admin -f "$BASE_URL/api/system/info"
}

# Wait for server to be up
PING=`isUp`
while [ -z "$PING" ]
do
  sleep 5
  PING=`isUp`
done

echo 'Sonar server running.'
echo 'Configuring quality profile'
# Restore qualityprofile
curl -v -u admin:admin -F "backup=@customprofile.xml" -X POST "$BASE_URL/api/qualityprofiles/restore"
curl -v -u admin:admin -X POST "$BASE_URL/api/qualityprofiles/set_default?language=java&qualityProfile=Java-Custom"

wait
echo 'Quality profile ready.'
