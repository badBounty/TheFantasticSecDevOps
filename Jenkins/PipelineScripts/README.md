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

- Canfigurar las siguientes credenciales o variables de entorno.

	| Variable       | Descripcion                                                    |
	|----------------|----------------------------------------------------------------|
	| gitlab-apitoken| Token del repositorio badbounty                                |
	| gitlab-token   | Token del repositorio codigo                                   |
	| ssh-key-vm     | Key ssh para conectase a la VM donde se deployea el docker SAST|
	| ssh-key        | Key ssh del docker SAST                                        |
	| port           | Puerto ssh del docker SAST                                     |
	| repoURL        | URL repositorio del codigo                                     |
	| SASTIP         | IP donde se encuentra el docker SAST                           |
	| sonarport      | Puerto donde se ejecuta el docker de sonar                     |
	| dashboardIP    | IP donde se encuentra la maquina del dashboard                 |


### Pipeline-Main
Este script es el main del pipeline el cual crea cada stage


### Install-GitCheckout
Este script hace un pull del repositorio de Git establecido

#### Interfaz
##### runStage()
Metodo principal para omenzar con el pull del repositorio remoto


### Install-MavenDependencies
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
Metodo principal para acceder a la api y obtener las vulnerabilidades.
Una vez obtenidas las vulnerabilidades de la API de sonar se conecta con la API de dashboards para subirlas a elasticsearch.

##### getVulnerabilities()
Devuelve un diccionario (key-value) con las vulnerabilidades, agrupadas por regla, en el siguiente formato:
```JSON
{
	VulnRuleName : [[IssueMessage,AffectedResource,AffectedLine],[IssueMessage,AffectedResource,AffectedLine]]
}
```


### SAST-Fortify
Este script consume la api de Fortify On Demands, permitiendo lanzar un escaneo, y traer los resultados.

#### Interfaz
##### runStage(bsiToken, sourceCodePath)
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

##### init(strategy) 
Requiere un strategy, que puede ser Redmine o Jira, el cual expona el meotodo "createIssue" 
```groovy
def createIssue(def keyProject, def ruleName, def issueMessage, def affectedResource, def affectedLine, def siteJira)
```
##### runStage(site, keyProject, vulsJsonList) 
Metodo principal para acceder a la api, permite la creacion de un issue por cada vulnerabilidad que se itere. Requiere llamar a init para configurar el strategy, el site, el "key project" y una coleccion de vulnerabilidades a subir en formato:
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


### Build-Maven
Este script realiza la compilación y el build de la aplicación.

#### Interfaz
##### runStage()
Metodo principal para comenzar el build de la aplicación.


### Build-DockerBuild
Este script realiza el build de la imagen Docker de la aplicación.

#### Interfaz
##### runStage()
Metodo principal para comenzar el build image en Docker de la aplicación.


### Build-DockerPush
Este script realiza el push de la imagen Docker de la aplicación a un repositorio de DockerHub.

#### Interfaz
##### runStage()
Metodo principal para pushear la imagen Docker de la aplicación a un repositorio de DockerHub.


### Deploy-DockerRun
Este script se comunica a través de SHH a una máquina con Docker para deployar la aplicación en un contenedor.

#### Interfaz
##### runStage()
Metodo principal para comenzar con el Deploy de la aplicación.


### Notifier
Script que mandar notificaciones a Slack o Teams

#### Interfaz

##### init(strategy) 
Requiere un strategy, que puede ser Slack o Teams, el cual expona el meotodo "sendMessage" 
```groovy
def sendMessage(channel, color, message)
```
##### runStage(channel, color, message) 
Metodo principal para acceder a la api, permite la creacion de una notificacion. Requiere llamar a init para configurar el strategy y también especificar los parámetros channel, color y message:
