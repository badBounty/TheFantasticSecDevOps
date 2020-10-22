def runStage()
{
    try {

        def dockerUser = 'mojedalopez'
        def appname = 'webgoat'

        def dockerRun = "docker run -p 8080:8080 -d --name ${appname} ${dockerUser}/${appname}"

        sshagent(['ssh-deploy'])
        {
            sh "ssh -o StrictHostKeyChecking=no dtt@172.16.222.49 ${dockerRun} "
        }

    } 
    catch(Exception e) 
    {
         try 
         {

            def dockerRun = "/home/dtt/runDocker.sh ${appname} ${dockerUser}"

            sshagent(['ssh-deploy']) 
            {
                sh "ssh -o StrictHostKeyChecking=no dtt@172.16.222.49 ${dockerRun} "
            }
        } 
        catch(Exception d)
        {
            //TODO use notifier module
            slackSend color: 'danger', message: 'Stage: "Deploy": FAILURE'
            
            currentBuild.result = 'FAILURE'
            print('Stage: "Deploy": FAILURE')
            print(d.printStackTrace())
        }
    }
} 
return this