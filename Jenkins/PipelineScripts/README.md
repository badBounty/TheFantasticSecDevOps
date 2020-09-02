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

## Pipeline Inicial
Se debe optar por alguno de estos pipeline, segun el lenguaje de programación.
### Pipeline-Maven
Este script contine los steps para la ejecución del pipeline para Java Maven.
### Pipeline-Dotnet
Este script contine los steps para la ejecución del pipeline para C# DotNetCore.
### Pipeline-NodeJS
Este script contine los steps para la ejecución del pipeline para Node.JS

## 1. Stage: Obtención del repositorio
Es el primer paso, obtener el repositorio a utilizar, por eso para el primer "stage" utiliza el siguiente script:
### Install-GitCheckout
Este script hace un pull del repositorio de Git establecido
#### Interfaz
##### runStage()
Método principal para omenzar con el pull del repositorio remoto

## 2. Stage: Instalación de dependencias
En el segundo stage intalaremos las dependencias necesarias. Uno de estos script realiza la instalación de dependencias necesarias para buildear la aplicación. Se debe elegir uno de los siguientes, segun el lenguaje de programación:
### Install-MavenDependencies
Dependencias de Java con Maven. Requiere archivo pom.xml
#### Interfaz
##### runStage()
Método principal para comenzar con la instalación de depencias.
### Install-NodeDependencies
Este script realiza la instalación de dependencias necesarias para buildear la aplicación.
#### Interfaz
##### runStage()
Método principal para comenzar con la instalación de depencias.
### Install-DotnetDependencies
Este script realiza la instalación de dependencias necesarias para buildear la aplicación.
#### Interfaz
##### runStage()
Método principal para comenzar con la instalación de depencias.

## 3. Stage: SAST
En este stage se realizará el SAST. Debemos iniciar con "SAST-Deployment", seguido del anánlisis del código, finalizando con "SAST-SonarResults" el cual realizará la unificación de resultados, y con "SAST-Destroy" quien será el encargado de destruir el contenedar de SAST, para no consumir recursos.
### SAST-Deployment
Este script realiza el deployment de un contenedor SAST, con las herramientas necesarias y SonarQube.
#### Interfaz
##### runStage()
Método principal para realizar el deployment.
### SAST-SonarQube-Maven
Este script realiza la ejecución de análisis de código estático en el servidor de SonarQube configurado para Maven.
#### Interfaz
##### runStage()
Método principal para comenzar con el análisis de código estático en el servidor de SonarQube.
### SAST-SonarQube-Dotnet
Este script realiza la ejecución de análisis de código estático en el servidor de SonarQube configurado para Dotnet.
#### Interfaz
##### runStage()
Método principal para comenzar con el análisis de código estático en el servidor de SonarQube.
### SAST-NodeJS
Este script realiza la ejecución de análisis de código para NodeJS utilizando herramientas exclusivas para esta tecnología.  
Actualmente las herrmaientas usadas son:
* NodeJSScan
### SAST-Dotnet
Este script realiza la ejecución de análisis de código para Dotnet utilizando herramientas exclusivas para esta tecnología.  
Actualmente las herrmaientas usadas son:
* Security Code Scan
* Puma Security Rules
#### Interfaz
##### runStage()
Método principal para comenzar con el análisis de código estático en el servidor de SonarQube.
### SAST-SonarResults
Este script consume la api de sonar para obtener las vulnerabilidades detectadas.
Y posteriormente envia las vulnerabilidades al VM Orchestrator en el formato del archivo [PostFormat.json](PostFormat.json). **Este script debe ser invocado siempre, se use o no SAST-SonarQube**.

#### Interfaz
##### runStage()
Método principal para acceder a la api y obtener las vulnerabilidades.
Una vez obtenidas las vulnerabilidades de la API de sonar se conecta con la API de dashboards para subirlas a elasticsearch.

##### getVulnerabilities()
Devuelve un diccionario (key-value) con las vulnerabilidades, agrupadas por regla, en el siguiente formato:
```JSON
{
	VulnRuleName : [[IssueMessage,AffectedResource,AffectedLine],[IssueMessage,AffectedResource,AffectedLine]]
}
```
## 4. Stage: Build
Se compila y contruye la aplicación.
### Build-Maven
Este script realiza la compilación y el build de la aplicación.
#### Interfaz
##### runStage()
Método principal para comenzar el build de la aplicación.
### Build-DockerBuild
Este script realiza el build de la imagen Docker de la aplicación.
#### Interfaz
##### runStage()
Método principal para comenzar el build image en Docker de la aplicación.
### Build-DockerPush
Este script realiza el push de la imagen Docker de la aplicación a un repositorio de DockerHub.
#### Interfaz
##### runStage()
Método principal para pushear la imagen Docker de la aplicación a un repositorio de DockerHub.

## 5. Stage: Deploy
Stage de deplyment.
### Deploy-DockerRun
Este script se comunica a través de SHH a una máquina con Docker para deployar la aplicación en un contenedor.
#### Interfaz
##### runStage()
Método principal para comenzar con el Deploy de la aplicación.

## 6. Stage: Alertas
Este Stage es el final, dedicado a alertas, luego de pasar por todos los anteriores. El mismo puede ser invocado en stages anteriores sin problema, para ir informando a medidad que se avanza en el pipeline.
### Notifier
Script que manda notificaciones a Slack o Teams.
#### Interfaz
##### init(strategy) 
Requiere un strategy, que puede ser Slack o Teams, el cual expona el meotodo "sendMessage" 
```groovy
def sendMessage(channel, color, message)
```
##### runStage(channel, color, message) 
Método principal para acceder a la api, permite la creacion de una notificacion. Requiere llamar a init para configurar el strategy y también especificar los parámetros channel, color y message.