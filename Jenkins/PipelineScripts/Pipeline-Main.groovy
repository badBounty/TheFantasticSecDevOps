
def modules = [:]
pipeline {
    agent any
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {

                        git credentialsId: 'gitlab-apitoken', url: 'https://gitlab.com/NicolasOjedajava/THEF4/'

                        modules.first = load "Git-Checkout.groovy"
                        modules.second = load "InstallDependencies-Maven.groovy"
                        modules.third = load "SAST-SonarQube.groovy"
                        modules.fourth = load "SAST-SonarResults.groovy"
                        //modules.fifth = load "SAST-Fortify.groovy"
                        modules.sixth = load "Ticketing-Jira.groovy"
                        modules.seventh = load "Build-Maven.groovy"
                        modules.eighth = load "Docker-Build.groovy"
                        modules.ninth = load "Docker-Push.groovy"
                        modules.tenth = load "Docker-Deploy.groovy"
                        
                        slackSend color: 'good', message: 'Pulling script files from github'
                        slackSend color: 'good', message: 'Git Pulling: SUCCESS'
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        print(e.printStackTrace())
                        currentBuild.result = 'FAILURE'      
                        slackSend color: 'danger', message: 'An error occurred in the "Import scripts files from Git" stage' 
                        slackSend color: 'danger', message: "Git Pulling: FAILURE"
                        print('------Stage "Import scripts files from Git": FAILURE ------')
                    } // try-catch-finally
                } // script
            } // steps
        } // stage

        stage('Git-Checkout'){
            steps{
                script{
                    modules.first.runStage()
                }
            }
        }

        stage('InstallDependencies-Maven'){
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
        stage('Ticketing-Jira'){
            steps{
                script{
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

        stage('Docker-Build'){
            steps{
                script{
                    seventh.runStage()
                    //modules.eighth.runStage()
                }
            }
        }

        stage('Docker-Push'){
            steps{
                script{
                    eighth.runStage()
                    //modules.ninth.runStage()
                }
            }
        }

        stage('Docker-Deploy'){
            steps{
                script{
                    modules.tenth.runStage()
                }
            }
        }
    } // stages
} // pipeline