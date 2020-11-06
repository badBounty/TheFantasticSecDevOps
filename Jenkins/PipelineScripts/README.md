# Pipeline Script
Esta carpeta contiene los scripts para la ejecucion del pipeline de jenkins.

## Pre-requisistos:
- Tener corriendo el orquestador Jenkins.
- Configurar un webhook para el pipeline en cuestion.
- Instalar los siguientes plugins manualmente en Jenkins, Algunos requieren autenticacion para conectarse al servicio que consumen.
	- Authentication Tokens API Plugin
	- Bitbucket
	- Docker
	- Generic Webhook Trigger Plugin
	- Git
	- GitHub
	- GitHub Authentication plugin
	- Gitlab Authentication
	- HTTP Request
	- JIRA Pipeline Steps
	- Maven Integration
	- Pipeline Utility Steps
	- Slack Notification
	- SSH Agent Plugin

- Canfigurar las siguientes credenciales.

	| Variable             | Descripcion                                                    |
	|----------------------|----------------------------------------------------------------|
	| git-secpipeline-token| Token del repositorio TheFantasticSecDevOps                    |
	| git-code-token       | Token del repositorio codigo a analizar                        |
	| ssh-key-SAST-server  | Key SSH para conectase al server que tiene la imagen SAST      |
	| ssh-key-SAST-image   | Key SSH de la imagen de SAST                                   |
	| slack-secret         | token de slack, generado con la app Jenkins Slack para         |
 
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
### Install-NodeJSDependencies
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
En este stage se realizará el SAST. Debemos iniciar con **"SAST-Deployment"**, seguido del anánlisis del código, finalizando con **"SAST-Destroy"** quien será el encargado de destruir el contenedar de SAST, para no consumir recursos.  
Finalmente para notificar resultados al orchestrator, es necesario el stage **"SAST-PostResults"** y **"SAST-SendVulnsLog"** para enviar las vuls que no pasaro el whitelisting a Slack.  
Para el análisis de código se deben ejecutar primero los **"SAST-SonarQube-{Lang}"** según apliquen, y luego los **"SAST-{Lang}"**. Finalizando con **"SAST-RegexScanner"**. Para obtener los resultados de sonar, es necesario llamar a **"SAST-SonarResults"** el cual extrae los resultados de la API de Sonarqube.  

## 4.  Alertas
Este no es un stage sino un modulo. El mismo puede ser invocado en stages anteriores sin problema, para ir informando a medidad que se avanza en el pipeline.
### Notifier
Script que manda notificaciones a Slack o Teams.
#### Interfaz
##### init(strategy) 
Requiere un strategy, que puede ser Slack o Teams, el cual expone el meotodo "sendMessage" 
```groovy
def sendMessage(channel, color, message)
```
##### runStage(channel, color, message) 
Método principal para acceder a la api, permite la creacion de una notificacion. Requiere llamar a init para configurar el strategy y también especificar los parámetros channel, color y message.
