# Pipeline Script
Esta carpeta contiene los scripts para la ejecucion del pipeline de jenkins.

## Pre-requisistos:
- Tener corriendo el orquestador Jenkins.
- Instalar los siguientes plugins manualmente en Jenkins, Algunos requieren autenticacion para conectarse al servicio que consumen.
	- Slack Notification
	- HTTP Request
	- Docker
	- SSH Agent
	- Maven
	- Git
	- Github
	- Gitlab
	- BitBucket
	- Pipeline Steps Utils
	- Sonar Scanner
	- Jira-steps


### Pipeline-Main
Este script es el main del pipeline el cual crea cada stage

### Git_Checkout
Este script hace un pull del repositorio de Git establecido

#### Interfaz
##### runStage()
Metodo principal para omenzar con el pull del repositorio remoto


### MavenInstallDepedencies
Este script realiza la instalación de dependencias necesarias para buildear la aplicación.

#### Interfaz
##### runStage()
Metodo principal para comenzar con la instalación de depencias.


### SAST-SonarQube
Este script realiza la ejecución de análisis de código estático en el servidor de SonarQube configurado.

#### Interfaz
##### runStage()
Metodo principal para comenzar con el análisis de código estático en el servidor de SonarQube.

### SAST-SonarResults
Este script consume la api de sonar para obtener las vulnerabilidades detectadas.

#### Interfaz
##### runStage()
Metodo principal para acceder a la api y obtener las vulnerabilidades

##### getVulnerabilities()
Devuelve un diccionario (key-value) con las vulnerabilidades en el siguiente formato:
```JSON
{
	VulnRuleName : [IssueMessage,AffectedResource,AffectedLine]
}
```

### SAST-Fortify
Este script consume la api de Fortify On Demands, permitiendo lanzar un escaneo, y traer los resultados.

#### Interfaz
##### runStage()
Metodo principal para acceder a la api y obtener las vulnerabilidades
##### getFortifyResult()
```JSON
{
	VulnRuleName : [IssueMessage,AffectedResource,AffectedLine]
}
```

### Ticketing-Jira
Script de Jira, que permite crar tickets para cada vul que se le pase.

#### Interfaz
##### runStage(siteJira, keyProject, vulsJsonList) 
Metodo principal para acceder a la api de Jira, permite la creacion de un issue por cada vulnerabilidad que se itere. Requiere el site de Jira, el "key project" y una coleccion de vulnerabilidades a subir en formato:
```JSON
{
	VulnRuleName : [IssueMessage,AffectedResource,AffectedLine]
}
```
##### getIssues()
Devuelve un diccionario (key-value) con los issues creados el siguiente formato:
```JSON
{
	Id : [KeyProject,Type,Summary,Description,VulnRuleName, UrlIssue]
}
```

### MavenBuild
Este script realiza la compilación y el build de la aplicación.

#### Interfaz
##### runStage()
Metodo principal para comenzar el build de la aplicación.


### DockerBuild
Este script realiza el build de la imagen Docker de la aplicación.

#### Interfaz
##### runStage()
Metodo principal para comenzar el build image en Docker de la aplicación.


### DockerPush
Este script realiza el push de la imagen Docker de la aplicación a un repositorio de DockerHub.

#### Interfaz
##### runStage()
Metodo principal para pushear la imagen Docker de la aplicación a un repositorio de DockerHub.


### DockerDeploy
Este script se comunica a través de SHH a una máquina con Docker para deployar la aplicación en un contenedor.

#### Interfaz
##### runStage()
Metodo principal para comenzar con el Deploy de la aplicación.
