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
        stage('Git Checkout') {
            steps {
                script{
                    modules.first = load "Git_Checkout.groovy"
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


