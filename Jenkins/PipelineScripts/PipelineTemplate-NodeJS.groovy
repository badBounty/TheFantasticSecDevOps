import groovy.json.JsonSlurperClassic

def vulns = []

/*
FORMAT:
title = vuln[0]
description = vuln[1]
component = vuln[2]
line = vuln[3]
affected_code = vuln[4]
hash = vuln[5]
severity = vuln[6]
origin = vuln[7]
*/

def modules = [:]
def SkipBuild = 'NO'

pipeline 
{
    agent any
    environment 
    {
        
        branches = {BRANCH,BRANCH} //List of valid branches

        Code_Repo_URL = {CODE_REPO_URL}
        
        SAST_Server_IP = {SAST_SERVER_IP}
        SAST_Server_User = {SAST_SERVER_USER}
        SAST_Server_Repository_SAST_Path = {SAST_SERVER_REPOSITORY_SAST_PATH}
        SAST_Server_SSH_Port = {SAST_SERVER_SSH_PORT}
        
        Sonar_Token = ''
        Sonar_Port = {SONAR_PORT}
        
        Orchestrator_POST_URL = {ORCH_POST_URL}
        Orchestrator_START_URL = {ORCH_START_URL}
        Orchestrator_END_URL = {ORCH_END_URL}

        SlackChannel = {SLACK_CHANNEL}     
        
        sleepTimePostResults = {SleepTime} //This must be in seconds/minutes
        
        nucleiTagsExclusion = "" //Configurar dependiendo la tecnología del pipeline
        
        EmailPrivateRepo = {EMAIL_PRIVATE_REPO}
        
        //Los values seteados entre {} deben ser configurados y/o pedidos internamente.
    }
    
    //Jenkins Server REGION -----------------------------------------------------------
    
    stages {
        stage('Import-Jenkins-Scripts'){
            steps{
                script{

                    //Check if trigred branch is a valid branch.
                    /*
                    if(!(env.branches.split(',').contains(env.branch))) {
                        SkipBuild = 'YES'
                        print(SkipBuild)
                    }
                    */
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
                        modules.Dependencies_Replace = load "Jenkins/PipelineScripts/DependenciesReplaceNodeJS.groovy"
                        modules.Install_Dependecies = load "Jenkins/PipelineScripts/Install-NodeJSDependencies.groovy"

                        modules.SAST_Deployment = load "Jenkins/PipelineScripts/SAST-Deployment.groovy"
                        modules.SAST_Sonarqube = load "Jenkins/PipelineScripts/SAST-SonarQube.groovy"
                        modules.SAST_SonarResults = load "Jenkins/PipelineScripts/SAST-SonarResults.groovy"
                        modules.SAST_NodeJS = load "Jenkins/PipelineScripts/SAST-NodeJS.groovy"
                        modules.SAST_NPMAudit = load "Jenkins/PipelineScripts/SAST-NodeJS-NPMAudit.groovy"
                        modules.SAST_SCA = load "Jenkins/PipelineScripts/SAST-SCA-NodeJS.groovy"
                        modules.SAST_Cloning = load "Jenkins/PipelineScripts/SAST-Cloning.groovy"
                        modules.SAST_Dependencies = load "Jenkins/PipelineScripts/SAST-DependencyCheck.groovy"
                        modules.SAST_Destroy = load "Jenkins/PipelineScripts/SAST-Destroy.groovy"
                        modules.SAST_Nuclei = load "Jenkins/PipelineScripts/SAST-Nuclei.groovy"
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

        stage('Install-GitCheckout')
        {
            steps{
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.Install_GitCheckout.runStage(modules.Notifier)
                }
            }
        }
        
        stage('Dependencies-Replace')
        {
            steps{
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.Dependencies_Replace.runStage(modules.Notifier)
                }
            }
        }

        stage('Install-Dependencies')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.Install_Dependecies.runStage(modules.Notifier)
                }
            }
        }       
        
        //SAST REGION -----------------------------------------------------
        
        stage('SAST-Deployment')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_Deployment.runStage(modules.Notifier)
                }
            }
        }
        
        stage('SAST-Cloning'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_Cloning.runStage(modules.Notifier)
                }
            }
        }
        
        stage('SAST-Nuclei'){
            steps{
                script{
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_Nuclei.runStage(modules.Notifier, vulns)
                }
            }
        }
        
        stage('SAST-NPMAudit')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    
                    modules.SAST_NPMAudit.runStage(modules.Notifier, vulns)

                }
            }
        }
        
        stage('SAST-SCA-NodeJS')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    
                    modules.SAST_SCA.runStage(modules.Notifier)

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
        
        stage('SAST-SonarQube')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_Sonarqube.runStage(modules.Notifier)

                }
            }
        }

        stage('SAST-NodeJS'){
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    
                    modules.SAST_NodeJS.runStage(modules.Notifier, vulns)

                }
            }
        }
        
        stage('SAST-SonarResults')
        {
            steps
            {
                script
                {
                    if (SkipBuild == 'YES'){
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                    modules.SAST_SonarResults.runStage(modules.Notifier, vulns)

                }
            }
        }

        stage('SAST-Destroy')
        {
            steps
            {
                script
                {
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
    }
}
