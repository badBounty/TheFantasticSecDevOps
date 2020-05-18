
def modules = [:]
pipeline {
    agent any
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {

                        //Importings scripts from gitlab
                        git credentialsId: 'gitlab-apitoken', url: 'https://gitlab.com/NicolasOjedajava/THEF4/'

                        //Load sripts in collection
                        modules.first = load "Install-GitCheckout.groovy"
                        modules.second = load "Install-MavenDependencies.groovy"
                        modules.third = load "SAST-SonarQube.groovy"
                        modules.fourth = load "SAST-SonarResults.groovy"
                        modules.fifth = load "SAST-Fortify.groovy"
                        modules.sixth = load "Ticketing-Jira.groovy"
                        modules.seventh = load "Build-Maven.groovy"
                        modules.eighth = load "Build-DockerBuild.groovy"
                        modules.ninth = load "Build-DockerPush.groovy"
                        modules.tenth = load "Deploy-DockerRun.groovy"
                        modules.eleventh = load "Notifier.groovy"
                        modules.twelfth = load "Notifier-Slack.groovy"
                        
                        modules.eleventh.init(modules.twelfth)
                        modules.eleventh.sendMessage('','good','Pulling script files from github') 
                        modules.eleventh.sendMessage('','good','Git Pulling: SUCCESS') 
                        
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        print(e.printStackTrace())
                        currentBuild.result = 'FAILURE'      
                        modules.eleventh.sendMessage('','danger','An error occurred in the "Import scripts files from Git" stage') 
                        modules.eleventh.sendMessage('','danger',"Git Pulling: FAILURE") 

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

        stage('SAST-SonarQube'){
            steps{
                script{
                    modules.third.runStage()
                }
            }
        }

        stage('SAST-SonarResults'){
            steps{
                script{
                    modules.fourth.runStage()
                    vulsJsonList = modules.fourth.getVulnerabilities()
                }
            }
        }
/*
        stage('SAST-Fortify'){
            steps{
                script{
                    modules.fifth.runStage()
                }
            }
        }
*/
        stage('Ticketing'){
            steps{
                script{
                    //modules.sixth.init()
                    modules.sixth.runStage('team-1588778856415.atlassian.net', 'JENKTEST', vulsJsonList)
                }
            }
        }

        stage('Build-Maven'){
            steps{
                script{
                    modules.seventh.runStage()
                }
            }
        }

        stage('Build-DockerBuild'){
            steps{
                script{
                    seventh.runStage()
                    //modules.eighth.runStage()
                }
            }
        }

        stage('Build-DockerPush'){
            steps{
                script{
                    eighth.runStage()
                    //modules.ninth.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.tenth.runStage()
                }
            }
        }
    } // stages
} // pipeline