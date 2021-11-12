stage('Install-GitCheckout')
{
  //Work around para cuando git falla en hacer fetch de "origin" --> code 128.
  environment {
      gitBranch = "${env.branch}"
      repoName = {REPONAME}
  }
  steps{
      script
      {
          if (SkipBuild == 'YES'){
              currentBuild.result = 'SUCCESS'
              return
          }

          try{
              withCredentials([usernamePassword(credentialsId: 'git-code-token-manual-clone', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
              {
                sh 'git clone https://${USERNAME}:${PASSWORD}@bitbucket.org/{REPONAME}.git'
                dir({REPONAME}){
                    sh 'git checkout $gitBranch'
                }
              }

          }
          catch(Exception e){
              print(e.getMessage())
          }                
      }
    }
}
