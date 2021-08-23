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

- Configurar las siguientes credenciales en Jenkins (Manage Jenkins -> Manage Credentials). Estas credenciales deben ser solicitadas.

	|Tipo                         | Variable             | Descripcion                                                    |
	|-----------------------------|----------------------|----------------------------------------------------------------|
	|Username with password       | git-secpipeline-token| Token del repositorio TheFantasticSecDevOps                    |
	|Username with password       | git-code-token       | Usuario y password del repositorio codigo a analizar           |
	|SSH Username with private key| ssh-key-SAST-server  | Key SSH para conectase al server que tiene la imagen SAST      |
	|SSH Username with private key| ssh-key-SAST-image   | Key SSH de la imagen de SAST                                   |
	|Secret text                  | slack-secret         | token de slack, generado con la app Jenkins Slack              |
	|Username with password       | sonar-credentials    | Sonarqube credentials                                          |
 
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
Método principal para comenzar con el pull del repositorio remoto

## 2. Stage: Instalación de dependencias
En el segundo stage instalaremos las dependencias necesarias. Uno de estos script realiza la instalación de dependencias necesarias para buildear la aplicación. El pipeline establecido selecciona un script para realizar la instalación, según el lenguaje de programación:
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
En este stage se realizará el SAST. Debemos iniciar con **"SAST-Deployment"**, seguido del anánlisis del código, finalizando con **"SAST-Destroy"** quien será el encargado de destruir el contenedor de SAST una vez finalizados los análisis, para no consumir recursos.  
Para el análisis de código se deben ejecutar primero los **"SAST-SonarQube-{Lang}"** según apliquen, y luego los **"SAST-{Lang}"**. Finalizando con **"SAST-RegexScanner"**. Para obtener los resultados de sonar, es necesario llamar a **"SAST-SonarResults"** el cual extrae los resultados de la API de Sonarqube. 

## Tecnologías utilizadas:

### Java: 
- SonarQube, DependenciesCheck, RegexScanner.

### .Net Core:
- SonarQube, Puma Scan, SecurityCodeScan.VS2017, DependenciesCheck, RegexScanner.

**Puma Scan** busca vulns que se encuentran en OWASP top 10, SANS/CWE top 25 y otros patrones inseguros.
**SecurityCodeScan.VS2017** es un analizador estatico de seguridad en el codigo para .NET. Detecta patrones como SQLI, XSS, CSRF, XXE, etc.

Ambos son paquetes de NuGet.

### NodeJS:
- SonarQube, njsscan, DependenciesCheck, RegexScanner.

**njsscan** es una tool de SAST que permite encontrar patrones inseguros en aplicaciones node.js usando un matcheador de patrones de libsast (SAST genérico) y semgrep que es un buscador de patrones en la semántica del codigo.

## Todas las tecnologías utilizan:

- **SonarQube**: Es una plataforma open source que permite evaluar código fuente utilizando herramientas de SAST como Checkstyle, PMD (Programming Mistake Detector) o FindBugs para realizar el análisis.
- **DependenciesCheck**: Es un proyecto de OWASP, una herramienta SCA (Software Composition Analysis) que intenta detectar vulnerabilidades públicas divulgadas en las dependencias dentro de un código fuente. 
- **RegexScanner**: Es un archivo .py que toma un regex.json de base, el código a analizar y provee un output. Recorre el archivo regex.json de base y luego recorre cada archivo con extensión válida del source a analizar. En caso de que no haya un match entre el regex y la linea analizada, se añade a un diccionario el cual se escribe a un output al finalizar.
 
## 
 
Finalmente para notificar resultados al orchestrator, es necesario el stage **"SAST-PostResults"** y **"SAST-SendVulnsLog"** para enviar las vuls que no pasaron el whitelisting a Slack. El whitelisting se determina mediante la KB que se puede encontrar en Django (Observations). Un issue se normaliza para catalogar en la KB. Cuando no cataloga, es enviada en el stage **"SAST-SendVulnsLog"**.

## 4.  Alertas
Este no es un stage si no un modulo. El mismo puede ser invocado en stages anteriores sin problema, para ir informando a medida que se avance en el pipeline.
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
