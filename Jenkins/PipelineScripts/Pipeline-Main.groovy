
def modules = [:]
pipeline {
    agent any
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {
                        git credentialsId: 'gitlab-apitoken', 
                            url: 'https://gitlab.com/NicolasOjedajava/THEF4/'

                        modules.first = load "Git_Checkout.groovy"
                        modules.second = load "MavenInstallDepedencies.groovy"
                        modules.third = load "SAST-SonarQube.groovy"
                        modules.fourth = load "SonarResults.groovy"
                        modules.fifth = load "MavenBuild.groovy"
                        modules.sixth = load "DockerBuild.groovy"
                        modules.seventh = load "DockerPush.groovy"
                        modules.eighth = load "DockerDeploy.groovy"

                        slackSend color: 'good', message: "Pulling script files from github"
                        slackSend color: 'good', message: 'Git Pulling: SUCCESS'
                        print('------Stage "Import scripts files from Git": SUCCESS ------')

                    } catch(Exception e) {
                        currentBuild.result = 'FAILURE'      
                        slackSend color: 'danger', message: 'An error occurred in the "Import scripts files from Git" stage' 
                        slackSend color: 'danger', message: "Git Pulling: FAILURE"
                        print('------Stage "Import scripts files from Git": FAILURE ------')
                    } // try-catch-finally
                } // script
            } // steps
        } // stage

        stage('Git Checkout'){
            steps{
                script{
                    modules.first.runStage()
                }
            }
        }

        stage('Installing Dependencies'){
            steps{
                script{
                    modules.second.runStage()
                }
            }
        }

        stage('SonarQube analysis'){
            steps{
                script{
                    modules.third.runStage()
                }
            }
        }
/*
        stage('Sonar Results'){
            steps{
                script{
                    modules.fourth.runStage()
                }
            }
        }

        stage('Build'){
            steps{
                script{
                    modules.fifth.runStage()
                }
            }
        }

        stage('Docker Build Image'){
            steps{
                script{
                    modules.sixth.runStage()
                }
            }
        }

        stage('Docker Push Image'){
            steps{
                script{
                    modules.seventh.runStage()
                }
            }
        }

        stage('Docker Deploy'){
            steps{
                script{
                    modules.eighth.runStage()
                }
            }
        }
*/
    } // stages
} // pipeline




