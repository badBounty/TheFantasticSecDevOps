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
	| dashboardURL   | URL del endpoint Orchastrator al que subir las issues          |


### Pipeline-Java
Este script contine los steps para la ejecución del pipeline para Java Maven

### Pipeline-C#
Este script contine los steps para la ejecución del pipeline para C# DotNet

### Pipeline-Main
Este script contine los steps para la ejecución del pipeline para Node.JS


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
Y posteriormente envia las vulnerabilidades al VM Orchestrator en el formato del archivo [PostFormat.json](PostFormat.json)

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
### SAST-C#
Este script ejecuta las herramientas de análisis de código C#. (Puma y Security Code Scan)
Y posteriormente envia las vulnerabilidades al VM Orchestrator en el formato del archivo [PostFormat.json](PostFormat.json)

### SAST-NodeJS
Este script ejecuta la herramienta de análisis de código javascript. (NodeJSScan)
Y posteriormente envia las vulnerabilidades al VM Orchestrator en el formato del archivo [PostFormat.json](PostFormat.json)

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
