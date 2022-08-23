# The Fantastic SecDevOps
# Jenkins

## Contenido 

Esta carpeta contiene la configuración necesaria para levantar un contenedor docker con Jenkins y docker integrado parametrizando las tecnologias requeridas dentro del docker o utilizar Jenkins en una VM.

#### Tecnologías pre instaladas en la imagen de Jenkins

- JAVA
- Node.JS
- .Net Core

## Pre-requisitos

- Docker (instalar con sudo apt install docker.io)
- Privilegios de root

## Instalación con Docker

1) Clonar el repo y posicionarse en esta carpeta.
2) Buildear la imagen Docker. Indicando a traves de build arguments las tecnologías necesarias como se muestra a continuacion. (no elegir Java ya que da problemas, y la imagen base de jenkins ya tiene java)
```
docker image build -t secpipeline-jenkins . --build-arg JAVA=no --build-arg node=yes --build-arg Net=yes
```

#### Parámetros permitidos

| Tecnologia | Nombre del argumento |
|------------|----------------------|
| JAVA       | JAVA                 |
| Node.JS    | node                 |
| .Net Core  | Net                  |


3) Para correr la imagen Docker de Jenkins con Docker dentro, se necesita agregar un volumen al momento de ejecutar "docker run". Para correr la imagen de Docker localmente: 
```
docker run --name secpipeline-jenkins -p 8080:8080 secpipeline-jenkins
```
*Nota: Esto se hace para que los contenedores Docker, tanto del Host como el de la imagen Jenkins, compartan el daemon de ejecución de Docker.*

4) Si se cierra la ventana, no hay que levantar otro container hay que empezar el detenido, para eso usamos

```
docker container start secpipeline-jenkins
```
