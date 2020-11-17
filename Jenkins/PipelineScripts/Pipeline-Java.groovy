import groovy.json.JsonSlurperClassic
def modules = [:]
pipeline {
    agent any
    environment 
    {
        
        branches = 'develop,master' //List of valid branches

        Code_Repo_URL = 'https://LeonardoMarazzo@bitbucket.org/directvla/dtvweb.git'
        
        SAST_Server_IP = '192.168.0.238'
        SAST_Server_User = 'maxpowersi'
        SAST_Server_Repository_SAST_Path = '/home/maxpowersi/TheFantasticSecDevOps/SAST'
        SAST_Server_SSH_Port = 4222
        
        Sonar_Token = ''
        Sonar_Port = 9000
        
        Orchestrator_POST_URL = 'https://726b58897291.ngrok.io/add_code_vulnerability/'
        Orchestrator_START_URL = 'https://726b58897291.ngrok.io/start'
        Orchestrator_END_URL = 'https://726b58897291.ngrok.io/end'

    }

    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                        try {

                            if(!(env.branches.split(',').contains(env.branch))) {
                            SkipBuild = 'YES'
                            print(SkipBuild)
                        }
                        if (SkipBuild == 'YES'){
                            currentBuild.result = 'SUCCESS'
                            return
                        }

                        sh "rm -rf \$(pwd)/*"
        
                        //Importings scripts from gitlab
                        git credentialsId: 'git-secpipeline-token', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'

                        modules.Notifier = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.Notifier_Slack = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"

                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": INIT')

                        modules.Notifier.Init(modules.Notifier_Slack)

                        
                        //Load sripts in collection
                        modules.Intall_GitCheckout = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-MavenDependencies.groovy"
                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_SonarQube_Maven = load "Jenkins/PipelineScripts/SAST-SonarQube-Maven.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_Dependencies = load "Jenkins/PipelineScripts/SAST-Java-DependenciesCheck.groovy"
                        modules.SAST_Regex = load "Jenkins/PipelineScripts/SAST-RegexScanner.groovy"
                        modules.SAST_Destroy = load "Jenkins/PipelineScripts/SAST-Destroy.groovy"
                        modules.SAST_PostResults = load "Jenkins/PipelineScripts/SAST-PostResults.groovy"
                        modules.SAST_SendVulnsLog = load "Jenkins/PipelineScripts/SAST-SendVulnsLog.groovy"
                        
                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": SUCCESS')
                        print('Stage: "Import-Jenkins-Scripts": SUCCESS')
                        print(modules)
                    }
                    catch(Exception e)
                    {
                        print(modules)
                        currentBuild.result = 'FAILURE'
                        print('Stage: "Import-Jenkins-Scripts": FAILURE')
                    } // try-catch-finally
                } // script
            } // steps
        } // stage

        stage('Install-GitCheckout'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.Intall_GitCheckout.runStage()
                }
            }
        }

        stage('Install-Dependencies'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    Intall_modules.Install_Dependecies.runStage()
                }
            }
        }

        stage('SAST-Deployment'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_Deployment.runStage()
                }
            }
        }

        stage('SAST-SonarQube'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_SonarQube_Maven.runStage()
                }
            }
        }

        stage('SAST-DependenciesChecks')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    
                    modules.SAST_Dependencies.runStage(modules.Notifier, vulns)
                    

                }
            }
        }

        //No SAST-Maven
        
        stage('SAST-SonarResults'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_SonarResults.runStage()
                }
            }
        }

        stage('SAST-RegexScanner'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_Regex.runStage(modules.Notifier, vulns)
                }
            }
        }

        stage('SAST-Destroy'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_Destroy.runStage()
                }
            }
        }

        stage('SAST-PostResults')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_PostResults.runStage(modules.Notifier, vulns)

                }
            }
        }

        stage('SAST-SendVulnsLog')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_SendVulnsLog.runStage(modules.Notifier)
                }
            }
        }

        /*stage('Build'){
            steps{
                script{
                    modules.Build_Maven.runStage()
                }
            }
        }

        stage('Build-DockerBuild'){
            steps{
                script{
                    modules.Build_DockerBuild.runStage()
                }
            }
        }

        stage('Deploy-DockerRun'){
            steps{
                script{
                    modules.Deploy_DockerRun.runStage()
                }
            }
        }/*
        
    } // stages
} // pipeline
