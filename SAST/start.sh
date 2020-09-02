#!/bin/bash

#Los pasos a continuacion permiten crear la imagen de sonar correctamente creando tambien los perfiles.

#1) Configurar en el script configure.sh las variables githubusr y githubpasswd con usuario y contraseña, respectivamente, de una cuenta de github con acceso al repositorio [SonarSecurityRules](https://github.com/badBounty/SonarSecurityRules)

#2) Ejecutar el archivo start.sh de la siguiente manera para buildear y correr la imagen de sonar

#```
#start.sh {build | nobuild} {container name} {running sonar port} {ssh port}
#```

#Los pasos a continuacion permiten crear la imagen de sonar correctamente creando tambien los perfiles.

#1) Configurar en el script configure.sh las variables githubusr y githubpasswd con usuario y contraseña, respectivamente, de una cuenta de github con acceso al repositorio [SonarSecurityRules](https://github.com/badBounty/SonarSecurityRules)

#2) Ejecutar el archivo start.sh de la siguiente manera para buildear y correr la imagen de sonar

#```
#docker image build -t jenkins-docker
#start.sh {build | nobuild} {container name} {running sonar port} {ssh port}
#```


if [ "$#" != "3" ] ; then
  echo "Se esperaban 3 argumentos y se recibieron $#";
  exit 1;
fi

if [ "$1" = "build" ] ; then
  echo 'building image'
  docker build --no-cache -t sonar .
  echo 'Image built'
fi

docker container rm -f $2
docker run -d --name $2 -p $4:22 -p $3:9000 sonar
echo 'Container running'
echo 'Wait for server to be up'

BASE_URL=http://127.0.0.1:$3

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
