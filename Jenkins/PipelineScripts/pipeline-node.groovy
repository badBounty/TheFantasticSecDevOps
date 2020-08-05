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
                        modules.third = load "Jenkins/PipelineScripts/SAST-SonarQube.groovy"
                        modules.fourth = load "Jenkins/PipelineScripts/SAST-NodeJS.groovy"
                        
                        modules.fifth = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.sixth = load "Jenkins/PipelineScripts/SAST-Fortify.groovy"
                        modules.seventh = load "Jenkins/PipelineScripts/Ticketing-Jira.groovy"
                        modules.eighth = load "Jenkins/PipelineScripts/Build-node.groovy"
                        modules.ninth = load "Jenkins/PipelineScripts/Build-DockerBuild.groovy"
                        modules.tenth = load "Jenkins/PipelineScripts/Build-DockerPush.groovy"
                        modules.eleventh = load "Jenkins/PipelineScripts/Deploy-DockerRun.groovy"
                        modules.twelfth = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.thirteenth = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"
                        
                        modules.twelfth.init(modules.thirteenth)
                        modules.twelfth.sendMessage('','good','Pulling script files from github') 
                        modules.twelfth.sendMessage('','good','Git Pulling: SUCCESS')
                        
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        //print(e.printStackTrace())
                        currentBuild.result = 'FAILURE'      
                        modules.twelfth.sendMessage('','danger','An error occurred in the "Import scripts files from Git" stage') 
                        modules.twelfth.sendMessage('','danger',"Git Pulling: FAILURE")

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
                   modules.third.runStage()
                }
            }
        }

        stage('SAST-NodeJSScan'){
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

        stage('SAST-Fortify'){
            steps{
                script{
                    modules.sixth.runStage()
                }
            }
        }

        stage('Ticketing'){
            steps{
                script{
                    modules.seventh.runStage('team-1588778856415.atlassian.net', 'JENKTEST', vulsJsonList)
                }
            }
        }

        stage('Build-node'){
            steps{
                script{
                    modules.eighth.runStage()
                }
            }
        }

        stage('Build-DockerBuild'){
            steps{
                script{
                    eighth.runStage()
                }
            }
        }

        stage('Build-DockerPush'){
            steps{
                script{
                    ninth.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.eleventh.runStage()
                }
            }
        }
        
    } // stages
} // pipeline