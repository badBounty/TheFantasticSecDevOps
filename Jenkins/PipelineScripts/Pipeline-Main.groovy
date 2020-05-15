
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
                        modules.fifth = load "Ticketing-Jira.groovy"
                        modules.sixth = load "MavenBuild.groovy"
                        seventh = load "DockerBuild.groovy"
                        //eighth = load "DockerPush.groovy"
                        //modules.eighth = load "DockerPush.groovy"
                        //modules.eighth = load "DockerDeploy.groovy"
                        
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

        stage('Sonar Results'){
            steps{
                script{
                    modules.fourth.runStage()
                    vulsJsonList = modules.fourth.getVulnerabilities()
                }
            }
        }

        stage('Ticketing Jira'){
            steps{
                script{
                    modules.fifth.runStage('team-1588778856415.atlassian.net', 'JENKTEST', vulsJsonList)
                }
            }
        }
/*
        stage('Build'){
            steps{
                script{
                    modules.sixth.runStage()
                }
            }
        }

        stage('Docker Build Image'){
            steps{
                script{
                    seventh.runStage()
                    //modules.seventh.runStage()
                }
            }
        }

        stage('Docker Push Image'){
            steps{
                script{
                    eighth.runStage()
                    //modules.eighth.runStage()
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




