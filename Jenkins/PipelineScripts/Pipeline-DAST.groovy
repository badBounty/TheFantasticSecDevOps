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

        target = ""

        SAST_Server_IP = '10.0.114.12'
        SAST_Server_User = 'ubuntu'
        SAST_Server_SSH_Port = 4444
        
        SlackChannel = 'dtv-DAST'

        Orchestrator_POST_URL = 'https://10.0.114.13:4000/add_web_vulnerability/'

    }

    stages {
        stage('Import scripts files from Git'){
            steps{
                script{
                        
                        sh "rm -rf \$(pwd)/*"
        
                        //Importings scripts from gitlab
                        git credentialsId: 'git-secpipeline-token', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'

                        modules.Notifier = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.Notifier_Slack = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"

                        modules.Notifier.Init(modules.Notifier_Slack)
                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": INIT')

                        modules.DAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.ZAPScan = load "Jenkins/PipelineScripts/DAST-ZAPScan.groovy"
                        modules.DAST_Destroy = load "Jenkins/PipelineScripts/DAST-Destroy.groovy"
                         
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

        stage('DAST-deploy'){
            modules.DAST_Deployment.runStage(modules.notifier)
        }

        stage('DAST-ZAP'){
            modules.ZAPScan.runStage(modules.notifier)
        }

        stage('DAST-Destroy'){
            modules.DAST_Destroy.runStage(modules.notifier)
        }

        
    } // stages
} // pipeline