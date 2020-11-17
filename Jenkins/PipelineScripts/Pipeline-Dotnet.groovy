import groovy.json.JsonSlurperClassic

def vulns = []
def modules = [:]
def SkipBuild = 'NO'

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

        SlackChannel = 'dtv-dtvweb'
    }

    stages {
        stage('Import-Jenkins-Scripts'){
            steps{
                script{

                    //Check if trigred branch is a valid branch.
                    if(!(env.branches.split(',').contains(env.branch))) {
                        SkipBuild = 'YES'
                        print(SkipBuild)
                    }
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    try {

                        sh "rm -rf \$(pwd)/*"

                        git credentialsId: 'git-secpipeline-token', url: 'https://github.com/badBounty/TheFantasticSecDevOps.git'

                        modules.Notifier = load "Jenkins/PipelineScripts/Notifier.groovy"
                        modules.Notifier_Slack = load "Jenkins/PipelineScripts/Notifier-Slack.groovy"
                        modules.Notifier.Init(modules.Notifier_Slack)

                        modules.Notifier.sendMessage('','good','Stage: "Import-Jenkins-Scripts": INIT')                        

                        modules.Install_GitCheckout = load "Jenkins/PipelineScripts/Install-GitCheckout.groovy"
                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-DotNetDependecies.groovy"

                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_Sonarqube = load "Jenkins/PipelineScripts/SAST-SonarQube-Dotnet.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_DotNet = load "Jenkins/PipelineScripts/SAST-Dotnet.groovy"
                        modules.SAST_Destroy = load "Jenkins/PipelineScripts/SAST-Destroy.groovy"
                        //modules.SAST_Dependencies = load "Jenkins/PipelineScripts/SAST-DotNet-DependencyCheckNPMAudit.groovy"
                        //modules.SAST_RegexScanner = load "Jenkins/PipelineScripts/SAST-RegexScanner.groovy"

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
                    }
                }
            }
        }

        stage('Install-GitCheckout'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.Install_GitCheckout.runStage(modules.Notifier)
                }
            }
        }

        stage('Install-Dependencies')
        {
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.Install_Dependecies.runStage(modules.Notifier)
                }
            }
        }

        stage('SAST-Deployment')
        {
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

        /*stage('SAST-DependenciesChecks')
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
        }*/

        stage('SAST-SonarQube'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                   modules.SAST_SonarQube_DotNet.runStage(modules.Notifier)
                }
            }
        }

        stage('SAST-DotnetCore'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                   modules.SAST_DotNet.runStage(modules.Notifier, vulns)
                }
            }
        }

        /*stage('SAST-RegexScanner'){
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_RegexScanner.runStage(modules.Notifier, vulns)
                    
                }
            }
        }*/
        
        stage('SAST-SonarResults')
        {
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
                    if (SkipBuild == 'YES')
                    {
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_SendVulnsLog.runStage(modules.Notifier)
                }
            }
        }        
    }
}