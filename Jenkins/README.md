# TheFantasticSecDevOps
## Jenkins

### Contenido 

Esta carpeta contiene la configuración necesaria para levantar un contenedor docker con Jenkins y docker integrado o utulizar Jenkin en una VM.

Arch: Host -> Docker -> Jenkins -> Docker (Build)

### Pre-requisitos

- Ubuntu 18.01
- Docker
- Privilegios de root

### Instalacion con docker

1) Copiar el archivo Dockerfile en una carpeta y personalizarlo si se necesita.
2) Abrir una terminal en la carpeta creada.
3) Buildear la imagen Docker.
4) Subir dicha imagen de Docker a un repositorio de DockerHub para su uso.
5) Para correr la imagen Docker de Jenkins con Docker dentro, se necesita agragar un volumen al  momento de ejecutar "docker run". Ejemplo: 

```
docker run --name jenkins-docker -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock miRepositorioDockerHub/jenkins-docker
```
*Nota: Esto se hace para que los contenedores Docker, tanto del Host como el de la imagen Jenkins, compartan el daemon de ejecución de Docker.^*


###  Instalacion sin  docker (script ci-start.sh)

Este archivo un script de instalación para una vm o máquina host destinada a orquestar. Está pensando para correr con Vagrant o ejecutarlo a mano.
