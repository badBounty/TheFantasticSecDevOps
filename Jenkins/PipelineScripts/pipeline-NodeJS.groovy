def modules = [:]
pipeline {
    agent any
    environment {
        port =
        repoURL = 
        SASTIP = 
        sonarport = 
        dashboardURL = 
        sonartoken = 
    }
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {
        
                        //Importings scripts from gitlab
                        git credentialsId: 'gitlab-apitoken', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'
                        
                        //Load sripts in collection
                        modules.Install_GitCheckout = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-NodeDependencies.groovy"
                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_SonarQube = load "Jenkins/PipelineScripts/SAST-SonarQube.groovy"
                        modules.SAST_NodeJS = load "Jenkins/PipelineScripts/SAST-NodeJS.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_Destroy = load "Jenkins/PipelineScrips/SAST-Destroy.groovy"
                        modules.Build_NodeJS = load "Jenkins/PipelineScripts/Build-NodeJS.groovy"
                        modules.Build_DockerBuild = load "Jenkins/PipelineScripts/Build-DockerBuild.groovy"
                        modules.Build_DockerPush = load "Jenkins/PipelineScripts/Build-DockerPush.groovy"
                        modules.Build_DockerRun = load "Jenkins/PipelineScripts/Deploy-DockerRun.groovy"
                        modules.Notifier = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.Notifier_Slack = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"
                        
                        modules.Notifier.init(modules.Notifier_Slack)
                        modules.Notifier.sendMessage('','good','Pulling script files from github')
                        modules.Notifier.sendMessage('','good','Git Pulling: SUCCESS')
                        
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        //print(e.printStackTrace())
                        currentBuild.result = 'FAILURE'      
                        modules.Notifier.sendMessage('','danger','An error occurred in the "Import scripts files from Git" stage') 
                        modules.Notifier.sendMessage('','danger',"Git Pulling: FAILURE")

                        print('------Stage "Import scripts files from Git": FAILURE ------')
                    } // try-catch-finally
                } // script
            } // steps
        } // stage

        stage('Install-GitCheckout'){
            steps{
                script{
                    modules.Install_GitCheckout.runStage()
                }
            }
        }

        stage('Install-Dependencies'){
            steps{
                script{
                    modules.Install_Dependecies.runStage()
                }
            }
        }

        stage('SAST-Deployment'){
            steps{
                script{
                    modules.SAST_Deployment.runStage()
                }
            }
        }

        //No Sonarqube for NodeJS

        stage('SAST-NodeJS'){
            steps{
                script{
                   modules.SAST_NodeJS.runStage()
                }
            }
        }
        
        stage('SAST-SonarResults'){
            steps{
                script{
                    modules.SAST_SonarResults.runStage()
                    vulsJsonList = modules.SAST_SonarResults.getVulnerabilities()
                }
            }
        }

        stage('SAST-Destroy'){
            steps{
                script{
                    modules.SAST_Destroy.runStage()
                }
            }
        }

        stage('Build'){
            steps{
                script{
                    modules.Build_NodeJS.runStage()
                }
            }
        }

        stage('Build-DockerBuild'){
            steps{
                script{
                    Build_DockerBuild.runStage()
                }
            }
        }

        stage('Build-DockerPush'){
            steps{
                script{
                    Build_DockerPush.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.Build_DockerRun.runStage()
                }
            }
        }
        
    } // stages
} // pipeline