//https://www.microfocus.com/documentation/fortify-on-demand-jenkins-plugin/5.0/FoD_Jenkins_Plugin_Help_5.0.0/index.htm
//https://www.jenkins.io/doc/pipeline/steps/fortify-on-demand-uploader/

//global vars
vuls = [:]

def runStage(bsiToken, sourCodePath){
	fodStaticAssessment bsiToken: bsiToken, 
	entitlementPreference: 'SingleScanOnly', 
	inProgressScanActionType: 'CancelInProgressScan', 
	personalAccessToken: 'JiraOauthAccess', 
	remediationScanPreferenceType: 'NonRemediationScanOnly', 
	srcLocation: sourCodePath
}

def getResults(bsiToken){
	def response = fodPollResults bsiToken: bsiToken, personalAccessToken: 'FoDApiKey', pollingInterval: 10
}

def getFortifyResult(){
	getResults(bsiToken)
	return vuls
}

return this