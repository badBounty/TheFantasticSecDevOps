# Pipeline Scripts
Esta carpeta contiene los scripts para la ejecución del pipeline de jenkins. Los scripts que poseen el nombre **"Pipeline-{nombre}"** son los principales los cuales deben ser configurados en el Pipeline de Jenkins. 

## Pre-requisistos:
- Tener corriendo el orquestador Jenkins y crear un Pipeline.
- Configurar un webhook para el pipeline en cuestión (no obligatorio).
- Instalar los siguientes plugins manualmente en Jenkins, Algunos requieren autenticación para conectarse al servicio que consumen. Para instalar un plugin ir a **Manage Jenkins -> Manage Plugins**. Algunos plugins ya vienen instalados por defecto en Jenkins.
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

- Configurar las siguientes credenciales en Jenkins **(Manage Jenkins -> Manage Credentials)**. Estas credenciales **deben** ser solicitadas.

	|Tipo                         | Variable             | Descripción                                                    |
	|-----------------------------|----------------------|----------------------------------------------------------------|
	|Username with password       | git-secpipeline-token| Token del repositorio TheFantasticSecDevOps                    |
	|Username with password       | git-code-token       | Usuario y password del repositorio codigo a analizar           |
	|Username with password       | git-code-token-nodeJS| Usuario y password del repositorio codigo NodeJS a analizar    |
	|Username with password       | git-code-token-clone | Token del repositorio TheFantasticSecDevOps para pipeline      |
	|SSH Username with private key| ssh-key-SAST-server  | Key SSH para conectase al server que tiene la imagen SAST      |
	|SSH Username with private key| ssh-key-SAST-image   | Key SSH de la imagen de SAST                                   |
	|Secret text                  | slack-secret         | Token de slack, generado con la app Jenkins Slack              |
	|Username with password       | sonar-credentials    | Sonarqube credentials                                          |
 
## Pipeline Inicial
Se debe optar por alguno de estos pipeline, segun el lenguaje de programación. Para configurar un script dentro de un pipeline en Jenkins, al momento de crear un Pipeline en Jenkins **(New item -> Pipeline)**, le damos un nombre y cuando finalizamos entramos al Pipeline y seleccionamos Configure. Una vez dentro pegamos el Script que seleccionamos en la pestaña **General**, donde dice Pipeline. Seleccionamos la opción "Use Groovy Sandbox" y dejamos la opción "Pipeline script". Recordar que estos son templates, no es un script que se use, es decir, se debe copiar pegar y adaptar. Estos scripts no se usan en el pipeline como los otros que si se usan a través de git clone.
### Pipeline-JavaMaven
Este script contine los steps para la ejecución del pipeline para Java Maven.
### Pipeline-Dotnet
Este script contine los steps para la ejecución del pipeline para C# DotNetCore.
### Pipeline-NodeJS
Este script contine los steps para la ejecución del pipeline para Node.JS.
### Pipeline-C/C++
Este script contine los steps para la ejecución del pipeline para C y C++.

**Nota**: Tener en cuenta que los scrips **DEBEN** ser modificados en cuanto a las variables de entorno ya que estas deben ser solicitadas.

## 1. Stage: Obtención del repositorio
Es el primer paso, obtiene el repositorio a utilizar, por eso para el primer "stage" se utiliza el siguiente script:
### Install-GitCheckout
Este script hace un pull del repositorio de Git establecido.
#### Interfaz
##### runStage()
Método principal para comenzar con el pull del repositorio remoto.

## 2. Stage: Instalación de dependencias
En el segundo stage instalaremos las dependencias necesarias. Uno de estos script realiza la instalación de dependencias necesarias para buildear la aplicación. El pipeline establecido selecciona un script para realizar la instalación, según el lenguaje de programación:
### Install-MavenDependencies
Dependencias de Java con Maven. Requiere archivo pom.xml (Es un archivo denominado "Project Object Model" de tipo XML que contiene información del proyecto y configuraciones que utiliza Maven para buildear el proyecto).
#### Interfaz
##### runStage()
Método principal para comenzar con la instalación de dependencias.
### Install-NodeJSDependencies
Este script realiza la instalación de dependencias necesarias para buildear la aplicación.

**Nota**: De momento se encuentra suspendido este stage debido a lo conflictos que genera NPM para instalar dependencias.
#### Interfaz
##### runStage()
Método principal para comenzar con la instalación de dependencias.
### Install-DotnetDependencies
Este script realiza la instalación de dependencias necesarias para buildear la aplicación.
#### Interfaz
##### runStage()
Método principal para comenzar con la instalación de dependencias.

## 3. Stage: SAST
En este stage se realizará el SAST. Debemos iniciar con **"SAST-Deployment"**, seguido del anánlisis del código, finalizando con **"SAST-Destroy"** quien será el encargado de destruir el contenedor de SAST una vez finalizados los análisis, para no consumir recursos.  
Para el análisis de código se deben ejecutar primero los **"SAST-SonarQube-{Lang}"** según apliquen, y luego los **"SAST-{Lang}"**. Finalizando con **"SAST-Nuclei"**. Para obtener los resultados de sonar, es necesario llamar a **"SAST-SonarResults"** el cual extrae los resultados de la API de Sonarqube. 

**Nota**: No todos los pipelines corren los stages de SAST en el mismo orden. Tener en cuenta que SAST es un contenedor Docker en un servidor.

## Tecnologías utilizadas:

### Java: 
- SonarQube, DependencyCheck, Nuclei.

### .Net Core:
- SonarQube, Puma Scan, SecurityCodeScan.VS2017, DependencyCheck, Nuclei.

**Puma Scan** busca vulns que se encuentran en OWASP top 10, SANS/CWE top 25 y otros patrones inseguros.
**SecurityCodeScan.VS2017** es un analizador estatico de seguridad en el codigo para .NET. Detecta patrones como SQLI, XSS, CSRF, XXE, etc.

Ambos son paquetes de NuGet.

### NodeJS:
- SonarQube, njsscan, DependencyCheck, Nuclei.

**njsscan** es una tool de SAST que permite encontrar patrones inseguros en aplicaciones node.js usando un matcheador de patrones de libsast (SAST genérico) y semgrep que es un buscador de patrones en la semántica del codigo.

### C/C++: 
- Flawfinder, Nuclei.

**Flawfinder** es una tool de SAST que permite analizar código C y C++ y reporta posibles "flaws" ordenados por nivel de severidad. Utiliza una base de datos y matchea patrones con ella. Es compatible con CWE (Common Weakness Enumeration).

## Todas las tecnologías utilizan (de momento menos C/C++):

- **SonarQube**: Es una plataforma open source que permite evaluar código fuente utilizando herramientas de SAST como Checkstyle, PMD (Programming Mistake Detector) o FindBugs para realizar el análisis.
- **DependencyCheck**: Es un proyecto de OWASP, una herramienta SCA (Software Composition Analysis) que intenta detectar vulnerabilidades públicas divulgadas en las dependencias dentro de un código fuente. 
- **Nuclei**: Es un scanner de vulnerabilidades basado en templates en formato YAML, en los cuales se definen propiedades para el escaneo de Nuclei. Se puede proporcionar una lista de targets y múltiples templates.
 
## 
 
Finalmente para notificar resultados al orchestrator, es necesario el stage **"SAST-PostResults"** y **"SAST-SendVulnsLog"** para enviar las vuls que no pasaron el whitelisting a Slack. El whitelisting se determina mediante la KB que se puede encontrar en Django (Observations). Un issue se normaliza para catalogar en la KB. Cuando no cataloga, es enviada en el stage **"SAST-SendVulnsLog"**.

## 4.  Alertas
Este no es un stage si no un módulo. El mismo puede ser invocado en stages anteriores sin problema, para ir informando a medida que se avance en el pipeline.
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

### Integración de Slack a Jenkins
Para integrar Slack a Jenkins es necesario primero crear un **Workspace**. Una vez realizado, se debe crear a continuación un **channel**. Luego, dentro del channel en la pestaña "Integrations" hay que añadir una app y esa es Jenkins. 

![image](https://user-images.githubusercontent.com/39742600/130459118-e988bf02-5079-4d3d-89f7-77865a773d6f.png)

Una vez realizado este paso, el siguiente link contiene los pasos próximos necesarios (ver hasta paso 3 únicamente) para una integración correcta.  
 - https://www.baeldung.com/ops/jenkins-slack-integration

Al finalizar, es necesario probar la conexión para que en Slack al channel correspondiente Jenkins nos diga que está todo listo. Para esto, se debe ir a **Manage Jenkins -> Configure System** y dentro debemos situarnos en la parte de Slack, la cual contiene el Workspace para setear, la credencial para seleccionar (recordar que es un *slack-secret*), luego el **Default channel**, en el cual introduciremos el **nombre del channel** con su símbolo #, por ejemplo (#general). Se selecciona la casilla **"Custom slack app bot user"** y finalmente se prueba la conexión mediante el botón **"Test Connection"**. Una vez que la conexión dice "Success", Jenkins alerta por Slack y está correctamente configurado. 

**Nota**: Es importante tener en cuenta que también dentro del script del Pipeline a ejecutar, se debe setear en la variable de *Slack Channel* el canal (no es necesario el símbolo #).

##

## FAQ:

### Slack:

- **¿Por qué me dice error "not in channel" cuando pruebo la conexión?**  
	- Verificar que Jenkins esté integrado específicamente en el channel al cual se desea que Jenkins alerte. Hay que entrar al canal y fijarse que en la sección de apps se 	   encuentre la creada para Jenkins.

- **¿Para qué es el Token OAuth que se genera?**  
	- Ese Token es para configurar la credencial en Jenkins de *slack-secret*. Al momento de probar la conexión utiliza esa credencial para identificar el Workspace.

##
