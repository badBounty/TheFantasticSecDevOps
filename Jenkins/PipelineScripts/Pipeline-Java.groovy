import groovy.json.JsonSlurperClassic

def vulns = []
def modules = [:]
def SkipBuild = 'NO'
pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }
    environment 
    {
        
        branches = 'develop,master' //TODO this value must be get from webhook

        Code_Repo_URL = 'https://bitbucket.org/directvla/ott-site.git'
        
        SAST_Server_IP = '192.168.0.98'
        SAST_Server_User = 'maxpowersi'
        SAST_Server_Repository_SAST_Path = '/home/maxpowersi/TheFantasticSecDevOps/SAST'
        SAST_Server_SSH_Port = 4444
        
        Sonar_Token = ''
        Sonar_Port = 9005
        SlackChannel = 'dtv-ottsite'
        
        
        Orchestrator_POST_URL = 'https://ce3306c4c420.ngrok.io/add_code_vulnerability/'
        Orchestrator_START_URL = 'https://ce3306c4c420.ngrok.io/rcv_code_vulnerability_state/'
        Orchestrator_END_URL = 'https://ce3306c4c420.ngrok.io/rcv_code_vulnerability_state/'

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

                        modules.Notifier.Init(modules.Notifier_Slack)
                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": INIT')

                        

                        
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
                    modules.Intall_GitCheckout.runStage(modules.Notifier)
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
                    Intall_modules.Install_Dependecies.runStage(modules.Notifier)
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
                    modules.SAST_Deployment.runStage(modules.Notifier)
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
                    modules.SAST_SonarQube_Maven.runStage(modules.Notifier)
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
                    modules.SAST_SonarResults.runStage(modules.Notifier, vulns)
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
                    modules.SAST_Destroy.runStage(modules.Notifier)
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
        }*/
        
    } // stages
} // pipeline
