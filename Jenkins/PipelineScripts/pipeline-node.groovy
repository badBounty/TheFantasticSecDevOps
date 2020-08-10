def modules = [:]
pipeline {
    agent any
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {
        
                        //Importings scripts from gitlab
                        git credentialsId: 'gitlab-apitoken', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'
                        //Load sripts in collection
                        modules.first = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.second = load "Jenkins/PipelineScripts/Install-NodeDependencies.groovy"
                        modules.third = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.fourth = load "Jenkins/PipelineScripts/SAST-SonarQube.groovy"
                        modules.fifth = load "Jenkins/PipelineScripts/SAST-NodeJS.groovy"
                        modules.sixth = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.seventh = load "Jenkins/PipelineScrips/SAST-Destroy.groovy"
                        modules.eighth = load "Jenkins/PipelineScripts/SAST-Fortify.groovy"
                        modules.nineth = load "Jenkins/PipelineScripts/Ticketing-Jira.groovy"
                        modules.tenth = load "Jenkins/PipelineScripts/Build-node.groovy"
                        modules.eleventh = load "Jenkins/PipelineScripts/Build-DockerBuild.groovy"
                        modules.twelfth = load "Jenkins/PipelineScripts/Build-DockerPush.groovy"
                        modules.fourthteenth = load "Jenkins/PipelineScripts/Deploy-DockerRun.groovy"
                        modules.fourteenth = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.fifteenth = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"
                        
                        modules.fourteenth.init(modules.fifteenth)
                        modules.fourteenth.sendMessage('','good','Pulling script files from github') 
                        modules.fourteenth.sendMessage('','good','Git Pulling: SUCCESS')
                        
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        //print(e.printStackTrace())
                        currentBuild.result = 'FAILURE'      
                        modules.fourteenth.sendMessage('','danger','An error occurred in the "Import scripts files from Git" stage') 
                        modules.fourteenth.sendMessage('','danger',"Git Pulling: FAILURE")

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

        stage('Install-Dependencies'){
            steps{
                script{
                    modules.second.runStage()
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

        stage('SAST-NodeJSScan'){
            steps{
                script{
                   modules.fifth.runStage()
                }
            }
        }
        
        stage('SAST-SonarResults'){
            steps{
                script{
                    modules.sixth.runStage()
                    vulsJsonList = modules.sixth.getVulnerabilities()
                }
            }
        }

        stage('SAST-Fortify'){
            steps{
                script{
                    modules.eighth.runStage()
                }
            }
        }

        stage('Ticketing'){
            steps{
                script{
                    modules.nineth.runStage('team-1588778856415.atlassian.net', 'JENKTEST', vulsJsonList)
                }
            }
        }

        stage('Build-node'){
            steps{
                script{
                    modules.tenth.runStage()
                }
            }
        }

        stage('Build-DockerBuild'){
            steps{
                script{
                    tenth.runStage()
                }
            }
        }

        stage('Build-DockerPush'){
            steps{
                script{
                    eleventh.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.fourthteenth.runStage()
                }
            }
        }
        
    } // stages
} // pipeline