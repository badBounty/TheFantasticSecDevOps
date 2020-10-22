import groovy.json.JsonSlurperClassic
def modules = [:]
pipeline {
    agent any
    environment {
        
        repoURL = 'https://LeonardoMarazzo@bitbucket.org/directvla/dtvweb.git'
        branch = 'develop'
        port = 4222
        SASTIP = '192.168.0.236'
        sonarport = 9000
        repositoryFolder = '/home/maxpowersi/TheFantasticSecDevOps/SAST'
        dashboardURL = 'https://df5a2387398e.ngrok.io/add_code_vulnerability/'
        sonartoken = ''
        SASTVMUSER = 'maxpowersi'
    }
    stages {
        stage('Import-Jenkins-Scripts'){
            steps{
                script{
                    try {
                        
                        sh "rm -rf \$(pwd)/*"
    
                        modules.Notifier = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.Notifier_Slack = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"
                        modules.Notifier.init(modules.Notifier_Slack)

                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": INIT')

                        git credentialsId: 'gitlab-apitoken', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'
                        
                        //Load sripts in collection
                        modules.Install_GitCheckout = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-NodeJSDependencies.groovy"
                        
                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        
                        modules.SAST_Sonarqube = load "Jenkins/PipelineScripts/SAST-SonarQube-NodeJS.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_NodeJS = load "Jenkins/PipelineScripts/SAST-NodeJS.groovy"
                        modules.SAST_Dependencies = load "Jenkins/PipelineScripts/SAST-NodeJS-DependencyCheckNPMAudit.groovy"

                        modules.SAST_Destroy = load "Jenkins/PipelineScrips/SAST-Destroy.groovy"

                        //modules.Build_NodeJS = load "Jenkins/PipelineScripts/Build-NodeJS.groovy"
                        //modules.Build_DockerBuild = load "Jenkins//PipelineScripts/Build-DockerBuild.groovy"

                        //modules.Deploy_DockerRun.groovy = load "Jenkins/PipelineScripts/Deploy-DockerRun.groovy"

                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": SUCCESS')
                        print('Stage: "Import-Jenkins-Scripts": SUCCESS')
                    }
                    catch(Exception e)
                    {
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
                    modules.Notifier.sendMessage('','good','Stage: "Install-GitCheckout": INIT')

                    modules.Install_GitCheckout.runStage()

                    modules.Notifier.sendMessage('','good','Stage: "Install-GitCheckout": SUCCESS')
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
                    modules.Notifier.sendMessage('','good','Stage: "Install-Dependencies": INIT')

                    modules.Install_Dependecies.runStage()

                    modules.Notifier.sendMessage('','good','Stage: "Install-Dependencies": SUCCESS')
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
                    modules.Notifier.sendMessage('','good','Stage: "SAST-Deployment": INIT')

                    modules.SAST_Deployment.runStage()

                    modules.Notifier.sendMessage('','good','Stage: "SAST-Deployment": SUCCESS')
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
                    modules.Notifier.sendMessage('','good','Stage: "SAST-DependenciesChecks": INIT')

                    modules.SAST_Dependencies.runStage()

                    modules.Notifier.sendMessage('','good','Stage: "SAST-DependenciesChecks": SUCCESS')
                    print('Stage: "SAST-DependenciesChecks": SUCCESS')
                }
            }
        }
        
        stage('SAST-SonarQube'){
            steps{
                script{
                    modules.SAST_Sonarqube.runStage()
            }
        }

        stage('SAST-NodeJS'){
            steps
            {
                script
                {
                    modules.Notifier.sendMessage('','good','Stage: "SAST-NodeJS": INIT')

                    modules.SAST_NodeJS.runStage()

                    modules.Notifier.sendMessage('','good','Stage: "SAST-NodeJS": SUCCESS')
                    print('Stage: "SAST-NodeJS": SUCCESS')
                }
            }
        }
        
        stage('SAST-SonarResults')
        {
            steps
            {
                script
                {
                    modules.Notifier.sendMessage('','good','Stage: "SAST-SonarResults": INIT')

                    modules.SAST_SonarResults.runStage()
                    vulsJsonList = modules.SAST_SonarResults.getVulnerabilities()

                    modules.Notifier.sendMessage('','good','Stage: "SAST-SonarResults": SUCCESS')
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
                    modules.Notifier.sendMessage('','good','Stage: "SAST-Destroy": INIT')

                    modules.SAST_Destroy.runStage()

                    modules.Notifier.sendMessage('','good','Stage: "SAST-Destroy": SUCCESS')
                    print('Stage: "SAST-Destroy": SUCCESS')
                }
            }
        }

        stage('Build-NodeJS')
        {
            steps
            {
                script
                {
                    //modules.Notifier.sendMessage('','good','Stage: "Build-NodeJS": INIT')

                    //modules.Build_NodeJS.runStage()

                    //modules.Notifier.sendMessage('','good','Stage: "Build-NodeJS": SUCCESS')
                    //print('Stage: "Build-NodeJS": SUCCESS')
                }
            }
        }

        stage('DockerBuild')
        {
            steps
            {
                script
                {
                    //modules.Notifier.sendMessage('','good','Stage: "DockerBuild": INIT')

                    //modules.Build_DockerBuild.runStage()

                     //modules.Notifier.sendMessage('','good','Stage: "DockerBuild": SUCCESS')
                     //print('Stage: "DockerBuild": SUCCESS')
                }
            }
        }

        stage('Deploy')
        {
            steps
            {
                script
                {
                    //modules.Notifier.sendMessage('','good','Stage: "Deploy": INIT')

                    //modules.Deploy_DockerRun.runStage()

                     //modules.Notifier.sendMessage('','good','Stage: "Deploy": SUCCESS')
                    //print('Stage: "Deploy": SUCCESS')
                }
            }
        }
    }
}