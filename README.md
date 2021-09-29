# The Fantastic SecDevOps
Fantastic SecDevOps es una infraestructura "ready to go" compuesta por cuatro máquinas virtuales (físicas o puede ser una máquina y muchos contenedores) con el propósito principal de implementar uno o mas "pipelines" SecDevOps usando Jenkins como orquestador, un proceso de implementación, SAST y DAST.


## Máquinas virtuales
Las cuatro máquinas virtuales consisten en:
 - Jenkins
 - SAST
 - VM-Orchestrator
 - DAST

## Jenkins
Jenkins es una plataforma de automatización conocido por ser un Orquestador. Dentro de él se ejecutan Pipelines u otros procesos. Posee una plataforma Web y a su vez permite configuraciones múltiples, adición de plugins para diferentes servicios, manejo de credenciales, etc. En este caso, Jenkins actúa en un contenedor Docker dentro de un servidor específico.

## SAST
SAST es "Static Application Security Testing". Consiste (en este caso) de un conjunto de herramientas SAST que actúan dependiendo la tecnología del Pipeline que se requiera ejecutar. Todas las herramientas están incluidas dentro de un Dockerfile, el cual genera una imagen Docker que Jenkins utilizará para crear y eliminar contenedores en cada Pipeline. En este caso, SAST actúa como contenedor en un servidor específico.

## VM-Orchestrator

## DAST

## FAQ (Completar)
