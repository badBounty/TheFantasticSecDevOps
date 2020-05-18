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
	- Fortify on Demand


### Pipeline-Main
Este script es el main del pipeline el cual crea cada stage

### Git-Checkout
Este script hace un pull del repositorio de Git establecido

#### Interfaz
##### runStage()
Metodo principal para omenzar con el pull del repositorio remoto

### InstallDepedencies-Maven
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
##### runStage(bsiToken, sourCodePath)
Metodo principal para acceder a la api e inicializar la comunicacion. Realiza un escaneo para el codigo actual, recibiendo un zip con el codigo fuente, y un token para lanzar el escaneo.
##### getFortifyResult()
Obtiene el resultado del escaneo en el siguiente formato:
```JSON
{
	VulnRuleName : [IssueMessage,AffectedResource,AffectedLine]
}
```
*Nota: Si los resultados no estan, porque el escaneo no finalizo, será un proceso bloqueante hasta que el escaneo termine, es recomendable llamarlo al final de piepline.*

### Ticketing
Script que permite crar tickets para cada vul que se le pase.

#### Interfaz
##### runStage(strategy, site, keyProject, vulsJsonList) 
Metodo principal para acceder a la api, permite la creacion de un issue por cada vulnerabilidad que se itere. Requiere un strategy, que puede ser Redmine o Jira, el cual expona el meotod "createIssue" Requiere el site, el "key project" y una coleccion de vulnerabilidades a subir en formato:
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
*Nota: la firma de createIssue es:*
```groovy
def createIssue(def keyProject, def ruleName, def issueMessage, def affectedResource, def affectedLine, def siteJira)
```

### Build-Maven
Este script realiza la compilación y el build de la aplicación.

#### Interfaz
##### runStage()
Metodo principal para comenzar el build de la aplicación.


### Docker-Build
Este script realiza el build de la imagen Docker de la aplicación.

#### Interfaz
##### runStage()
Metodo principal para comenzar el build image en Docker de la aplicación.


### Docker-Push
Este script realiza el push de la imagen Docker de la aplicación a un repositorio de DockerHub.

#### Interfaz
##### runStage()
Metodo principal para pushear la imagen Docker de la aplicación a un repositorio de DockerHub.


### Docker-Deploy
Este script se comunica a través de SHH a una máquina con Docker para deployar la aplicación en un contenedor.

#### Interfaz
##### runStage()
Metodo principal para comenzar con el Deploy de la aplicación.