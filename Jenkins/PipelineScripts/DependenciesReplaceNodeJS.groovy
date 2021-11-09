def runStage(notifier)
{
    try
    {
        notifier.sendMessage('','good','Stage: "Dependencies-Replace-NodeJS": INIT')

        withCredentials([usernamePassword(credentialsId: 'git-code-token-nodeJS', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
        {
            sh 'sed -i "s/bitbucket/${USERNAME}:${PASSWORD}@bitbucket/g" package.json'
        }

        notifier.sendMessage('','good','Stage: "Dependencies-Replace-NodeJS": SUCCESS')
        print('Stage: "Dependencies-Replace-NodeJS": SUCCESS')
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "Dependencies-Replace-NodeJS": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "Dependencies-Replace-NodeJS": FAILURE')
    } 
}

return this
