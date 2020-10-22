def modules = [:]
pipeline {
    agent any
    environment {
        
        repoURL = 'https://LeonardoMarazzo@bitbucket.org/directvla/dtvweb.git'
        branch = 'develop'
        port = 4222
        SASTIP = '192.168.0.236'
        sonarport = 9000
        repositoryFolder = '/home/maxpowersi/TheFantasticSecDevOps/SAST'
        dashboardURL = 'https://df5a2387398e.ngrok.io/add_code_vulnerability/'
        sonartoken = ''
        SASTVMUSER = 'maxpowersi'
    }
    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                    try {
                        
                        sh "rm -rf \$(pwd)/*"
        
                        //Importings scripts from gitlab
                        git credentialsId: 'gitlab-apitoken', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'
                        
                        //Load sripts in collection
                        modules.Install_GitCheckout = load "/var/jenkins_home/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "/var/jenkins_home/PipelineScripts/Install-NodeDependencies.groovy"
                        modules.SAST_Deployment = load "/var/jenkins_home/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_Dependencies = load "/var/jenkins_home/PipelineScripts/NodeDependencyCheckNPMAudit.groovy"
                        modules.SAST_Sonarqube = load "/var/jenkins_home/PipelineScripts/SAST-SonarQube-Node.groovy"
                        modules.SAST_NodeJS = load "/var/jenkins_home/PipelineScripts/SAST-NodeJS.groovy"

                        modules.SAST_Destroy = load "/var/jenkins_home/PipelineScripts/SAST-Destroy.groovy"
                        
                        modules.Build_NodeJS = load "/var/jenkins_home/PipelineScripts/Build-NodeJS.groovy"
                        //modules.Build_DockerBuild = load "/var/jenkins_home/PipelineScripts/Build-DockerBuild.groovy"
                        //modules.Build_DockerRun = load "/var/jenkins_home/PipelineScripts/Deploy-DockerRun.groovy"
                        
                        print(modules)
                        
                        print('------Stage "Import scripts files from Git": SUCCESS ------')
                    } catch(Exception e) {

                        //print(e.printStackTrace())
                        currentBuild.result = 'FAILURE'      
                        
                        print('------Stage "Import scripts files from Git": FAILURE ------')
                    } // try-catch-finally
                } // script
            } // steps
        } // stage

        stage('Install-GitCheckout'){
            steps{
                script{
                    modules.Install_GitCheckout.runStage()
                }
            }
        }
        
        stage('Dependencies replace'){
            steps{
                script{
                    try {
                        withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]){
                            sh 'sed -i "s/bitbucket/${USERNAME}:${PASSWORD}@bitbucket/g" package.json'
                        }
                		
                		print('------Stage "Dependencies replace": SUCCESS ------')
                
                	} catch(Exception e) {

                		currentBuild.result = 'FAILURE'    
                		print('------Stage "Dependencies replace": FAILURE ------')
                
                	} // try-catch-finally
                }
            }
        }

        stage('Install-Dependencies'){
            steps{
                script{
                    modules.Install_Dependecies.runStage()
                }
            }
        }

        stage('SAST-Deployment'){
            steps{
                script{
                    
                    modules.SAST_Deployment.runStage()
                }
            }
        }
        
        stage('SAST-DependenciesChecks'){
            steps{
                script{
                    modules.SAST_Dependencies.runStage()
                }
            }
        }
        
        stage('SAST-SonarQube'){
            steps{
                script{
                    modules.SAST_Sonarqube.runStage()
                }
            }
        }

        //No Sonarqube for NodeJS

        stage('SAST-NodeJS'){
            steps{
                script{
                    modules.SAST_NodeJS.runStage()
                }
            }
        }
        
        stage('SAST-SonarResults'){
            steps{
                script{
                    modules.SAST_SonarResults.runStage()
                    vulsJsonList = modules.SAST_SonarResults.getVulnerabilities()
                }
            }
        }

        stage('SAST-Destroy'){
            steps{
                script{
                    modules.SAST_Destroy.runStage()
                }
            }
        }

        stage('Build'){
            steps{
                script{
                    modules.Build_NodeJS.runStage()
                }
            }
        }

        /*stage('Build-DockerBuild'){
            steps{
                script{
                    Build_DockerBuild.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.Build_DockerRun.runStage()
                }
            }
        }/*
        
    } // stages
} // pipeline