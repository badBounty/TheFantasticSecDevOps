import groovy.json.JsonSlurperClassic

def modules = [:]
pipeline 
{
    agent any
    environment 
    {
        branch = 'develop' //TODO this value must be get from webhook

        //Repo to get jenkins scripts
        repoURL = 'https://LeonardoMarazzo@bitbucket.org/directvla/dtvweb.git'

        //Host with SAST image IP and username
        SASTIP = '192.168.0.236' // Host IP
        SASTVMUSER = 'maxpowersi' // Username the access is using Priv Key config in Jenkins
        repositoryFolder = '/home/maxpowersi/TheFantasticSecDevOps/SAST' //Start script to deploy SAST

        //This is inside the SAST images must be fixed here
        sonartoken = '' //sonar token, the auth is open, TODO: Use token
        sonarport = 9000 //sonar port
        port = 4222 //SSH Port in Sonar container
        
        //VM orch to post results
        dashboardURL = 'https://192.168.0.100/add_code_vulnerability/'
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
                        modules.Install_GitCheckout.Init(modules.Notifier)

                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-NodeJSDependencies.groovy"
                        modules.Install_Dependecies.Init(modules.Notifier)

                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_Deployment.Init(modules.Notifier)

                        modules.SAST_Sonarqube = load "Jenkins/PipelineScripts/SAST-SonarQube-NodeJS.groovy"
                        modules.SAST_Sonarqube.Init(modules.Notifier)

                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_SonarResults.Init(modules.Notifier)

                        modules.SAST_NodeJS = load "Jenkins/PipelineScripts/SAST-NodeJS.groovy"
                        modules.SAST_NodeJS.Init(modules.Notifier)

                        modules.SAST_Dependencies = load "Jenkins/PipelineScripts/SAST-NodeJS-DependencyCheckNPMAudit.groovy"
                        modules.SAST_Dependencies.Init(modules.Notifier)

                        modules.SAST_RegexScanner = load "Jenkins/PipelineScripts/SAST-RegexScanner.groovy"
                        modules.SAST_RegexScanner.Init(modules.Notifier)

                        modules.SAST_Destroy = load "Jenkins/PipelineScripts/SAST-Destroy.groovy"
                        modules.SAST_Destroy.Init(modules.Notifier)

                        modules.VulnsLog = load "Jenkins/PipelineScripts/sendVulnsLog.groovy"
                        modules.VulnsLog.Init(modules.Notifier)

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

                    modules.Install_GitCheckout.runStage()

                    print('Stage: "Install-GitCheckout": SUCCESS')
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
                    modules.Install_Dependecies.runStage()

                    print('Stage: "Install-Dependencies": SUCCESS')
                }
            }
        }

        stage('SAST-Deployment')
        {
            steps
            {
                script
                {
                    modules.SAST_Deployment.runStage()
                    
                    print('Stage: "Install-Dependencies": SUCCESS')
                }
            }
        }
        
        stage('SAST-DependenciesChecks')
        {
            steps
            {
                script
                {
                    
                    modules.SAST_Dependencies.runStage()
                    
                    print('Stage: "SAST-DependenciesChecks": SUCCESS')
                }
            }
        }
        
        stage('SAST-SonarQube')
        {
            steps
            {
                script
                {
                    modules.SAST_Sonarqube.runStage()
                    
                    print('Stage: "SAST-SonarQube": SUCCESS')
                }
            }
        }

        stage('SAST-NodeJS'){
            steps
            {
                script
                {
                    
                    modules.SAST_NodeJS.runStage()
                    
                    print('Stage: "SAST-NodeJS": SUCCESS')
                }
            }
        }

        stage('SAST-RegexScanner'){
            steps
            {
                script
                {
                    modules.SAST_RegexScanner.runStage()
                    
                    print('Stage: "SAST-RegexScanner": SUCCESS')
                }
            }
        }
        
        stage('SAST-SonarResults')
        {
            steps
            {
                script
                {
                    modules.SAST_SonarResults.runStage()
                    
                    print('Stage: "SAST-SonarResults": SUCCESS')
                }
            }
        }

        stage('SAST-Destroy')
        {
            steps
            {
                script
                {
                    modules.SAST_Destroy.runStage()
                    
                    print('Stage: "SAST-Destroy": SUCCESS')
                }
            }
        }

        stage('Vulns Filtered Log')
        {
            steps
            {
                script
                {
                    modules.VulnsLog.runStage()
                    
                    print('Stage: "SAST-Destroy": SUCCESS')
                }
            }
        }
    }
}