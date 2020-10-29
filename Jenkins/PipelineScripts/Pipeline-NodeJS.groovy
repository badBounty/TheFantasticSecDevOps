import groovy.json.JsonSlurperClassic

def vulns = []
def modules = [:]
pipeline 
{
    agent any
    environment 
    {
        branch = 'develop' //TODO this value must be get from webhook

        Code_Repo_URL = 'https://LeonardoMarazzo@bitbucket.org/directvla/dtvweb.git'
        
        SAST_Server_IP = '192.168.0.238'
        SAST_Server_User = 'maxpowersi'
        SAST_Server_Repository_SAST_Path = '/home/maxpowersi/TheFantasticSecDevOps/SAST'
        SAST_Server_SSH_Port = 4222
        
        Sonar_Token = ''
        Sonar_Port = 9000
        
        Orchestrator_POST_URL = 'https://192.168.0.100/add_code_vulnerability/'
    }
    stages {
        stage('Import-Jenkins-Scripts'){
            steps{
                script{
                    try {
                        
                        sh "rm -rf \$(pwd)/*"

                        git credentialsId: 'gitlab-apitoken', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'

    
                        modules.Notifier = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.Notifier_Slack = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"
                        modules.Notifier.Init(modules.Notifier_Slack)

                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": INIT')

                        modules.Install_GitCheckout = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-NodeJSDependencies.groovy"
                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_Sonarqube = load "Jenkins/PipelineScripts/SAST-SonarQube-NodeJS.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_NodeJS = load "Jenkins/PipelineScripts/SAST-NodeJS.groovy"
                        modules.SAST_Dependencies = load "Jenkins/PipelineScripts/SAST-NodeJS-DependencyCheckNPMAudit.groovy"
                        modules.SAST_RegexScanner = load "Jenkins/PipelineScripts/SAST-RegexScanner.groovy"
                        modules.SAST_Destroy = load "Jenkins/PipelineScripts/SAST-Destroy.groovy"
                        modules.SAST_PostResults = load "Jenkins/PipelineScripts/SAST-PostResults.groovy"
                        modules.SAST_SendVulnsLog = load "Jenkins/PipelineScripts/SAST-SendVulnsLog.groovy"

                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": SUCCESS')
                        print('Stage: "Import-Jenkins-Scripts": SUCCESS')
                        print(modules)
                    }
                    catch(Exception e)
                    {
                        print(modules)
                        currentBuild.result = 'FAILURE'
                        print('Stage: "Import-Jenkins-Scripts": FAILURE')
                    }
                }
            }
        }

        stage('Install-GitCheckout')
        {
            steps{
                script
                {
                    modules.Install_GitCheckout.runStage(modules.Notifier)
                }
            }
        }
        
        //Work around for DTV and NodeJS, this stage set credentials in order to acces private repo in package.json
        stage('Dependencies-Replace')
        {
            steps{
                script
                {
                    try
                    {
                        modules.Notifier.sendMessage('','good','Stage: "Dependencies-Replace": INIT')
                        
                        withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
                        {
                          sh 'sed -i "s/bitbucket/${USERNAME}:${PASSWORD}@bitbucket/g" package.json'
                        }
                        
                        modules.Notifier.sendMessage('','good','Stage: "Dependencies-Replace": SUCCESS')
                		print('Stage: "Dependencies-Replace": SUCCESS')
                	}
                    catch(Exception e)
                    {
                        modules.Notifier.sendMessage('','danger','Stage: "Dependencies-Replace": FAILURE')
                		currentBuild.result = 'FAILURE'
                		print('Stage: "Dependencies-Replace": FAILURE')
                	} 
                }
            }
        }

        stage('Install-Dependencies'){
            steps
            {
                script
                {
                    modules.Install_Dependecies.runStage(modules.Notifier)
                }
            }
        }

        stage('SAST-Deployment')
        {
            steps
            {
                script
                {
                    modules.SAST_Deployment.runStage(modules.Notifier)

                }
            }
        }
        
        stage('SAST-DependenciesChecks')
        {
            steps
            {
                script
                {
                    
                    modules.SAST_Dependencies.runStage(modules.Notifier, vulns)

                }
            }
        }
        
        stage('SAST-SonarQube')
        {
            steps
            {
                script
                {
                    modules.SAST_Sonarqube.runStage(modules.Notifier)

                }
            }
        }

        stage('SAST-NodeJS'){
            steps
            {
                script
                {
                    
                    modules.SAST_NodeJS.runStage(modules.Notifier, vulns)

                }
            }
        }

        stage('SAST-RegexScanner'){
            steps
            {
                script
                {
                    modules.SAST_RegexScanner.runStage(modules.Notifier, vulns)
                    
                }
            }
        }
        
        stage('SAST-SonarResults')
        {
            steps
            {
                script
                {
                    modules.SAST_SonarResults.runStage(modules.Notifier, vulns)

                }
            }
        }

        stage('SAST-Destroy')
        {
            steps
            {
                script
                {
                    modules.SAST_Destroy.runStage(modules.Notifier)

                }
            }
        }

        stage('SAST-PostResults')
        {
            steps
            {
                script
                {
                    modules.SAST_PostResults.runStage(modules.Notifier, vulns)

                }
            }
        }

        stage('SAST-SendVulnsLog')
        {
            steps
            {
                script
                {
                    modules.SAST_SendVulnsLog.runStage(modules.Notifier)
                }
            }
        }
    }
}