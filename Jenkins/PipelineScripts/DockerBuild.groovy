def runStage(){

    def dockerUser = 'mojedalopez'
    def appName = 'webgoat'

    try {
        
        dockerImage = docker.build("${dockerUser}/${appName}", "./webgoat-server")
        slackSend color: 'good', message: 'Docker Image Build: SUCCESS ' 
        print('------Stage "Docker Image Build": SUCCESS ------')

    } catch(Exception e) {

        currentBuild.result = 'FAILURE'    
        slackSend color: 'danger', message: 'An error occurred in the "Docker Image Build" stage' 
        print('--------Stage "Docker Image Build": FAILURE --------')
    } // try-catch-finally 
} // stage('Docker Image Build')

return this