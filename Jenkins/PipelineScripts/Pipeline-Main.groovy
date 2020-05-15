
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
                    modules.first = load "Git_Checkout.groovy"
                    modules.first.runStage()
                }
            }
        }
/*
        stage('Installing Dependencies'){
            steps{
                script{
                    modules.second = load "MavenInstallDepedencies.groovy"
                    modules.second.runStage()
                }
            }
        }

        stage('Installing Dependencies'){
            steps{
                script{
                    modules.second = load "SAST-SonarQube.groovy"
                    modules.second.runStage()
                }
            }
        }

        stage('Build'){
            steps{
                script{
                    modules.second = load "MavenBuild.groovy"
                    modules.second.runStage()
                }
            }
        }

        stage('Docker Build Image'){
            steps{
                script{
                    modules.second = load "DockerBuild.groovy"
                    modules.second.runStage()
                }
            }
        }

        stage('Docker Push Image'){
            steps{
                script{
                    modules.second = load "DockerPush.groovy"
                    modules.second.runStage()
                }
            }
        }

        stage('Docker Deploy'){
            steps{
                script{
                    modules.second = load "DockerDeploy.groovy"
                    modules.second.runStage()
                }
            }
        }
*/
    } // stages
} // pipeline




