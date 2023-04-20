def modules = [:]
pipeline {
    agent any
    environment {
        //Scripts:
        REPO_FANTASTIC_NAME = 'TheFantasticSecDevOps'
        REPO_FANTASTIC_TOKEN = 'git-secpipeline-token'
        REPO_FANTASTIC_URL = 'https://github.com/badBounty/TheFantasticSecDevOps.git'
        REPO_FANTASTIC_BRANCH = 'testing'
        //Repository to scan:
        REPO_TO_SCAN_NAME = 'roku-native'
        REPO_TO_SCAN_TOKEN_ID = 'git-code-token-manual-clone'
        REPO_TO_SCAN_URL = 'bitbucket.org/directvla/roku-native.git'
        REPO_TO_SCAN_BRANCH = 'master'
        //SAST - Server:
        SAST_SERVER_IP = '10.0.114.12'
        SAST_SERVER_USERNAME = 'ubuntu'
        SAST_SERVER_SCRIPTS_SAST_PATH = '/home/ubuntu/TheFantasticSecDevOps/SAST'
        SAST_SERVER_SSH_PORT = 4222
            //Scanners:
                //SonarQube:
                    SONAR_TOKEN = ''
                    SONAR_PORT = 9000
                    INSIDER_TECHNOLOGY = 'php'
                //Nuclei:
                    NUCLEI_TAGS_EXCLUSION = "xss,android,js-analyse,php"
                //Semgrep:
                    SEMGREP_RULE = 'php'
        //Notification (must be configured earlier with call me api)
        NOTIF_NUMBER = '+5491132617901'
        NOTIF_TOKEN = '439147'
    }
    stages {
        stage('Clean-Up') {
            steps {
                deleteDir()
            }
        }
        stage('Importing TheFantasticSecDevOps scripts') {
            steps {
                script {
                    try {
                        git credentialsId: "${env.REPO_FANTASTIC_TOKEN}", url: "${env.REPO_FANTASTIC_URL}"
                        sh "git checkout ${REPO_FANTASTIC_BRANCH}"
                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"                    
                        modules.SAST_SonarQube = load "Jenkins/PipelineScripts/SAST-SonarQube.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_Dependencies = load "Jenkins/PipelineScripts/SAST-DependencyCheck.groovy"
                        modules.SAST_Nuclei = load "Jenkins/PipelineScripts/SAST-Nuclei.groovy"
                        modules.SAST_Semgrep = load "Jenkins/PipelineScripts/SAST-Semgrep.groovy"
                        modules.SAST_Insider = load "Jenkins/PipelineScripts/SAST-Insider.groovy"
                        modules.SAST_Cloning = load "Jenkins/PipelineScripts/SAST-Cloning.groovy"
                        modules.SAST_Sca = load "Jenkins/PipelineScripts/SAST-SCA-NodeJS.groovy"
                        modules.SAST_Destroy = load "Jenkins/PipelineScripts/SAST-Destroy.groovy"
                        print(modules)
                        deleteDir()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }      
                }   
            } 
        }
        stage('Importing Repository to scan') {
            steps {
                script {
                    try {
                        withCredentials([usernamePassword(credentialsId: "${env.REPO_TO_SCAN_TOKEN_ID}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
                        {
                            sh "git clone https://${USERNAME}:${PASSWORD}@${env.REPO_TO_SCAN_URL}"
                            dir(REPO_TO_SCAN_NAME) {
                                sh "git checkout ${REPO_TO_SCAN_BRANCH}"
                            }
                        }
                    }    
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }        
                }
            }
        }
        stage('SAST-Deployment') {
            steps {
                script {
                    try {
                        modules.SAST_Deployment.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }   
        stage('SAST-Cloning') {
            steps {
                script {
                    try {
                        modules.SAST_Cloning.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        
        stage('SAST-Nuclei') {
            steps {
                script {
                    try {
                        modules.SAST_Nuclei.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        
        stage('SAST-NodeJS') {
            steps {
                script {
                    try {
                        modules.SAST_NodeJS.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        
        stage('SAST-Semgrep') {
            steps {
                script {
                    try {
                        modules.SAST_Semgrep.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }   
                }
            }
        }
        
        stage('SAST-SCA-NodeJS') {
            steps {
                script {
                    try {
                        modules.SAST_Sca.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        
        stage('SAST-DependenciesChecks') {
            steps {
                script {
                    try {
                        modules.SAST_Dependencies.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        
        stage('SAST-SonarQube') {
            steps {
                script {
                    try {
                        modules.SAST_Sonarqube.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        
        stage('SAST-SonarResults') {
            steps {
                script {
                    try {
                        modules.SAST_SonarResults.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        stage('SAST-Destroy') {
            steps {
                script {
                    try {
                        modules.SAST_Destroy.runStage()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        stage('SAST-Whatsapp-Notification') {
            steps {
                script {
                    try {
                        sh "curl -Ik 'https://api.callmebot.com/whatsapp.php?phone=${env.NOTIF_NUMBER}&text=Termino+la+ejecucion+de+${env.REPO_TO_SCAN_NAME}&apikey=${env.NOTIF_TOKEN}'"
                        deleteDir()
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                    }
                }
            }
        }
    }
}

