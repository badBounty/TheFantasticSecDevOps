# Pipeline Script
Esta carpeta contiene los scripts para la ejecucion del pipeline de jenkins.

## pipeline.groovy
Este script es el main del pipeline el cual crea cada stage, 


## SAST-SonarResults
Este script consume la api de sonar para obtener las vulnerabilidades detectadas.

### Interfaz
#### runStage()
Metodo principal para acceder a la api y obtener las vulnerabilidades

#### getVulnerabilities()
Devuelve un diccionario con las vulnerabilidades en el siguiente formato:
```JSON
{
	VulnRuleNAME: [
		IssueMessage,
		AffectedResource,
		AffectedLine		
	]
}
```
## SAST-Fortify
Este script consume la api de Fortify On Demands, permitiendo lanzar un escaneo, y traer los resultados.

### Interfaz
TODO

## Ticketing-Jira
Script de Jira, que permite crar tickets para cada vul que se le pase.

### Interfaz
TODO