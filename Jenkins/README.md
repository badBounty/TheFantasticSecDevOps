# TheFantasticSecDevOps
## Jenkins

### Dockerfile 

Este archivo contiene la configuración necesaria para levantar un contenedor docker con Jenkins y docker integrado.

Arch: Host -> Docker -> Jenkins -> Docker (Build)

##### Pre-requisitos:

- Un servidor Linux/Windows con Docker instalado. Es recomendable Ubuntu 18.01
- Privilegios de Root/Administrador en la máquina host.
- Conocimientos básicos de comandos Docker.

##### Uso:

1) Copiar el archivo Dockerfile en una carpeta y personalizarlo si se necesita.
2) Abrir una terminal en la carpeta creada.
3) Buildear la imagen Docker.
4) Subir dicha imagen de Docker a un repositorio de DockerHub para su uso.
5) Para correr la imagen Docker de Jenkins con Docker dentro, se necesita agragar un volumen al  momento de ejecutar "docker run". 

Ej:  docker run --name jenkins-docker -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock miRepositorioDockerHub/jenkins-docker

Esto se hace para que los contenedores Docker, tanto del Host como el de la imagen Jenkins, compartan el daemon de ejecución de Docker.


### ci-start.sh

Este archivo un script de instalación para una vm o máquina host destinada a orquestar. Está pensando para correr con Vagrant o ejecutarlo a mano.

##### Pre-requisitos:

- Un servidor Linux Ubuntu 18.01.
- Privilegios de Root.
- Darle permisos de ejecución al archivo "ci-start.sh".

#### Contenido

-última versión de Jenkins.
-Java 1.8 y 11.
-Última versión de Docker.
-Configuración inicial de firewall para el Lab.
