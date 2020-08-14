
def modules = [:]
pipeline {
    agent any
    environment {
        port =
        repoURL = 
        SASTIP = 
        sonarport = 
        dashboardURL = 
    }
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {

                        //Importings scripts from gitlab
                        git credentialsId: 'gitlab-apitoken', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'

                        //Load sripts in collection
                        modules.first = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.second = load "Jenkins/PipelineScripts/Install-MavenDependencies.groovy"
                        modules.third = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.fourth = load "Jenkins/PipelineScripts/SAST-SonarQube.groovy"
                        modules.fifth = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.sixth = load "Jenkins/PipelineScrips/SAST-Destroy.groovy"
                        modules.seventh = load "Jenkins/PipelineScripts/SAST-Fortify.groovy"
                        modules.eighth = load "Jenkins/PipelineScripts/Ticketing-Jira.groovy"
                        modules.nineth = load "Jenkins/PipelineScripts/Build-Maven.groovy"
                        modules.tenth = load "Jenkins/PipelineScripts/Build-DockerBuild.groovy"
                        modules.eleventh = load "Jenkins/PipelineScripts/Build-DockerPush.groovy"
                        modules.twelfth = load "Jenkins/PipelineScripts/Deploy-DockerRun.groovy"
                        modules.fourthteenth = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.fourteenth = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"
                        
                        modules.fourthteenth.init(modules.fourteenth)
                        modules.fourthteenth.sendMessage('','good','Pulling script files from github') 
                        modules.fourthteenth.sendMessage('','good','Git Pulling: SUCCESS') 
                        
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        print(e.printStackTrace())
                        currentBuild.result = 'FAILURE'      
                        modules.fourthteenth.sendMessage('','danger','An error occurred in the "Import scripts files from Git" stage') 
                        modules.fourthteenth.sendMessage('','danger',"Git Pulling: FAILURE") 

                        print('------Stage "Import scripts files from Git": FAILURE ------')
                    } // try-catch-finally
                } // script
            } // steps
        } // stage

        stage('Install-GitCheckout'){
            steps{
                script{
                    modules.first.runStage()
                }
            }
        }

        stage('Install-MavenDependencies'){
            steps{
                script{
                    modules.second.runStage()
                }
            }
        }

        stage('SAST-Deployment'){
            steps{
                script{
                    modules.third.runStage()
                }
            }
        }


        stage('SAST-SonarQube'){
            steps{
                script{
                    modules.fourth.runStage()
                }
            }
        }

        stage('SAST-SonarResults'){
            steps{
                script{
                    modules.fifth.runStage()
                    vulsJsonList = modules.fifth.getVulnerabilities()
                }
            }
        }

        stage('SAST-Destroy'){
            steps{
                script{
                    modules.sixth.runStage()
                }
            }
        }

        stage('SAST-Fortify'){
            steps{
                script{
                    modules.seventh.runStage()
                }
            }
        }

        

        stage('Ticketing'){
            steps{
                script{
                    //modules.eighth.init()
                    modules.eighth.runStage('team-1588778856415.atlassian.net', 'JENKTEST', vulsJsonList)
                }
            }
        }

        stage('Build-Maven'){
            steps{
                script{
                    modules.nineth.runStage()
                }
            }
        }

        stage('Build-DockerBuild'){
            steps{
                script{
                    nineth.runStage()
                    //modules.tenth.runStage()
                }
            }
        }

        stage('Build-DockerPush'){
            steps{
                script{
                    tenth.runStage()
                    //modules.eleventh.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.twelfth.runStage()
                }
            }
        }
    } // stages
} // pipeline