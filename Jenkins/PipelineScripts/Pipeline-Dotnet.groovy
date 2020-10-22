import groovy.json.JsonSlurperClassic
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
                        sh "rm -rf \$(pwd)/*"
        
                        //Importings scripts from gitlab
                        git credentialsId: 'gitlab-apitoken', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'

                        //Load sripts in collection
                        modules.Install_GitCheckout = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-DotNetDependecies.groovy"

                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"

                        modules.SAST_Sonarqube = load "Jenkins/PipelineScripts/SAST-SonarQube-Dotnet.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_DotNet = load "Jenkins/PipelineScripts/SAST-Dotnet.groovy"

                        modules.SAST_Destroy = load "Jenkins/PipelineScrips/SAST-Destroy.groovy"
                        
                        //modules.Build_Dotnet = load "/var/jenkins_home/PipelineScripts/Build-Dotnet.groovy"
                        //modules.Build_DockerBuild = load "/var/jenkins_home/PipelineScripts/Build-DockerBuild.groovy"
                        
                        //modules.Deploy_DockerRun = load "/var/jenkins_home/PipelineScripts/Deploy-DockerRun.groovy"

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

        stage('SAST-SonarQube'){
            steps{
                script{
                   modules.SAST_SonarQube_DotNet.runStage()
                }
            }
        }

        stage('SAST-Dotnet#'){
            steps{
                script{
                   modules.SAST_DotNet.runStage()
                   modules.SAST_DotNet.parseVulns()
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
                    modules.Build_Dotnet.runStage()
                }
            }
        }
        
        /*stage('Build-DockerBuild'){
            steps{
                script{
                    modules.Build_DockerBuild.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.Deploy_DockerRun.runStage()
                }
            }
        }*/
        
    } // stages
} // pipeline