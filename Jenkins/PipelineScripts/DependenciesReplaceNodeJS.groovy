def runStage(notifier)
{
    try
    {
        notifier.sendMessage('','good','Stage: "Dependencies_Replace": INIT')

        withCredentials([usernamePassword(credentialsId: 'git-code-token-nodeJS', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
        {
            sh 'sed -i "s/bitbucket/${USERNAME}:${PASSWORD}@bitbucket/g" package.json'
        }

        notifier.sendMessage('','good','Stage: "Dependencies_Replace": SUCCESS')
        print('Stage: "Dependencies_Replace": SUCCESS')
    }
    catch(Exception e)
    {
        notifier.sendMessage('','danger','Stage: "Dependencies_Replace": FAILURE')
        currentBuild.result = 'FAILURE'
        print('Stage: "Dependencies_Replace": FAILURE')
    } 
}

return this
