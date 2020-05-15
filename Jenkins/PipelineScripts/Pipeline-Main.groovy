/*

def git_username = 'NicolasOjedajava'
def git_password = ''


File file = new File("/var/jenkins_home/pipelinesScripts/Git_Checkout.groovy")

if (file.exists()){
    sh ('git clone')
} else {
    sh ('git pull')
}
*/

def modules = [:]
pipeline {
    agent any
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {
                        git credentialsId: 'gitlab-apitoken', 
                            url: 'https://github.com/badBounty/TheFantasticSecDevOps/'
                            
                        slackSend color: 'good', message: "Pulling script files from github"
                        slackSend color: 'good', message: 'Git Pulling: SUCCESS'
                        print('------Stage "environment config": SUCCESS ------')

                    } catch(Exception e) {
                        currentBuild.result = 'FAILURE'      
                        slackSend color: 'danger', message: 'An error occurred in the "Import scripts files from Git" stage' 
                        slackSend color: 'danger', message: "Git Pulling: FAILURE"
                        print('------Stage "environment config": FAILURE ------')
                    } // try-catch-finally
                }
            }
        }

        stage('Git Checkout') {
            steps {
                script{
                    modules.first = load "./TheFantasticSecDevOps/Jenkins/PipelineScripts/Git_Checkout.groovy"
                    modules.first.runStage()
                }
            }
        }
/*
        stage('Installing Dependencies') {
            steps {
                script{
                    modules.second = load "MavenInstallDepedencies.groovy"
                    modules.second.runStage()
                }
            }
        }

        stage('Installing Dependencies') {
            steps {
                script{
                    modules.second = load "SAST-SonarQube.groovy"
                    modules.second.runStage()
                }
            }
        }
*/
    }
}


