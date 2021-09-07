# The Fantastic SecDevOps
# Jenkins

## Contenido 

Esta carpeta contiene la configuración necesaria para levantar un contenedor docker con Jenkins y docker integrado parametrizando las tecnologias requeridas dentro del docker o utilizar Jenkins en una VM.

Arch: Host -> Docker -> Jenkins -> Docker (Build)

#### Tecnologías soportadas

- JAVA
- Node.JS
- .Net Core

## Pre-requisitos

- Ubuntu 18.01
- Docker
- Privilegios de root

## Instalación con Docker

1) Copiar el archivo Dockerfile en una carpeta y personalizarlo si se necesita.
2) Abrir una terminal en la carpeta creada.
3) Buildear la imagen Docker. Indicando a traves de build arguments las tecnologías necesarias como se muestra a continuacion.
```
docker image build -t secpipeline-jenkins . --build-arg JAVA=yes --build-arg node=yes --build-arg Net=yes
```

#### Parámetros permitidos

| Tecnologia | Nombre del argumento |
|------------|----------------------|
| JAVA       | JAVA                 |
| Node.JS    | node                 |
| .Net Core  | Net                  |


4) Para correr la imagen Docker de Jenkins con Docker dentro, se necesita agregar un volumen al momento de ejecutar "docker run". Para correr la imagen de Docker localmente: 
```
docker run --name secpipeline-jenkins -p 8080:8080 secpipeline-jenkins
```
   En caso de que Docker determine que la imagen no puede ser encontrada, revisar el nombre de la misma, porque a veces puede quedar con el nombre "jenkins/jenkins". En ese caso: 
```
docker run --name secpipeline-jenkins -p 8080:8080 jenkins/jenkins
```
*Nota: Esto se hace para que los contenedores Docker, tanto del Host como el de la imagen Jenkins, compartan el daemon de ejecución de Docker.*

## FAQ (Completar)
