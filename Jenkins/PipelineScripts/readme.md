# Pipeline Script
Esta carpeta contiene los scripts para la ejecucion del pipeline de jenkins.

## Pipeline-Main
Este script es el main del pipeline el cual crea cada stage


## SAST-SonarResults
Este script consume la api de sonar para obtener las vulnerabilidades detectadas.

### Interfaz
#### runStage()
Metodo principal para acceder a la api y obtener las vulnerabilidades

#### getVulnerabilities()
Devuelve un diccionario (key-value) con las vulnerabilidades en el siguiente formato:
```JSON
{
	VulnRuleName : [IssueMessage,AffectedResource,AffectedLine]
}
```
## Ticketing-Jira
Script de Jira, que permite crar tickets para cada vul que se le pase.

### Interfaz
#### runStage(vulsJsonList, keyProject)
Metodo principal para acceder a la api de Jira, permite la creacion de un issue por cada vulnerabilidad que se itere. Requiere el "key project" de Jira, y una coleccion de vulnerabilidades a subir en formato:
```JSON
{
	VulnRuleName : [IssueMessage,AffectedResource,AffectedLine]
}
```
#### getIssues()
Devuelve un diccionario (key-value) con los issues creados el siguiente formato:
```JSON
{
	Id : [KeyProject,Type,Summary,Description,VulnRuleName,]
}
```

## SAST-Fortify
Este script consume la api de Fortify On Demands, permitiendo lanzar un escaneo, y traer los resultados.

### Interfaz
TODO