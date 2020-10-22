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

                        
                        //Load sripts in collection
                        modules.Intall_GitCheckout = load "/var/jenkins_home/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "/var/jenkins_home/PipelineScripts/Install-MavenDependencies.groovy"
                        modules.SAST_Deployment = load "/var/jenkins_home/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_SonarQube_Maven = load "/var/jenkins_home/PipelineScripts/SAST-SonarQube-Maven.groovy"
                        modules.SAST_SonarResults = load "/var/jenkins_home/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_Destroy = load "/var/jenkins_home/PipelineScrips/SAST-Destroy.groovy"
                        modules.Build_Maven = load "/var/jenkins_home/PipelineScripts/Build-Maven.groovy"
                        modules.Build_DockerBuild = load "/var/jenkins_home/PipelineScripts/Build-DockerBuild.groovy"
                        modules.Deploy_DockerRun = load "/var/jenkins_home/PipelineScripts/Deploy-DockerRun.groovy"
                        modules.Notifier = load "/var/jenkins_home/PipelineScripts/Notifier.groovy"
                        modules.Notifier_Slack = load "/var/jenkins_home/PipelineScripts/Notifier-Slack.groovy"
                        
                        modules.Notifier.init(modules.Notifier_Slack)

                        modules.Notifier.sendMessage('','good','Pulling script files from github') 
                        modules.Notifier.sendMessage('','good','Git Pulling: SUCCESS') 
                        
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        print(e.printStackTrace())
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
                    modules.Intall_GitCheckout.runStage()
                }
            }
        }

        stage('Install-Dependencies'){
            steps{
                script{
                    Intall_modules.Install_Dependecies.runStage()
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
                    modules.SAST_SonarQube_Maven.runStage()
                }
            }
        }

        //No SAST-Maven
        
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
                    modules.Build_Maven.runStage()
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
        }/*
        
    } // stages
} // pipeline