//https://www.microfocus.com/documentation/fortify-on-demand-jenkins-plugin/5.0/FoD_Jenkins_Plugin_Help_5.0.0/index.htm
//https://www.jenkins.io/doc/pipeline/steps/fortify-on-demand-uploader/

//global vars
vuls = [:]

def runStage(bsiToken, sourCodePath){
	fodStaticAssessment bsiToken: bsiToken, 
	entitlementPreference: 'SingleScanOnly', 
	inProgressScanActionType: 'CancelInProgressScan',  
	remediationScanPreferenceType: 'NonRemediationScanOnly', 
	srcLocation: sourCodePath
}

def getResults(bsiToken){
	def response = fodPollResults bsiToken: bsiToken, pollingInterval: 10
}

def getFortifyResult(){
	getResults(bsiToken)
	return vuls
}

return this

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

