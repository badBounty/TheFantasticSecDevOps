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

- Canfigurar las siguientes credenciales o variables de entorno.

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
En este stage se realizará el SAST. Debemos iniciar con **"SAST-Deployment"**, seguido del anánlisis del código, finalizando con **"SAST-SonarResults"** el cual realizará la unificación de resultados, y con **"SAST-Destroy"** quien será el encargado de destruir el contenedar de SAST, para no consumir recursos.  
Para el análisis de código se deben ejecutar primero los **"SAST-SonarQube-{Lang}"** según apliquen, y luego los **"SAST-{Lang}"**.
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
### SAST-SonarQube-NodeJs
Este script realiza la ejecución de análisis de código estático en el servidor de SonarQube configurado para NodeJS.
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
Y posteriormente envia las vulnerabilidades al VM Orchestrator en el formato del archivo [PostFormat.json](PostFormat.json). **Cada script SAST usa este formato para enviar la info al orquestador**.

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

## 4.  Alertas
Este Stage es el final, dedicado a alertas, luego de pasar por todos los anteriores. El mismo puede ser invocado en stages anteriores sin problema, para ir informando a medidad que se avanza en el pipeline.
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