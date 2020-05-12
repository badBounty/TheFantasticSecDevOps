# Pipeline Script

Esta carpeta contiene los scripts para la ejecucion del pipeline de jenkins.

## SonarAPI

Este script consume la api de sonar para obtener las vulnerabilidades detectadas.

#### runStage()

Metodo principal para acceder a la api y obtener las vulnerabilidades

#### getVulnerabilities

Devuelve un diccionario con las vulnerabilidades en el siguiente formato:
{
	VulnRuleNAME: [
		IssueMessage,
		AffectedResource,
		AffectedLine		
	]
	
}
