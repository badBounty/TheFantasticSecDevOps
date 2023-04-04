stage('SAST-Whatsapp-Notification')
{
  environment {
      repoName = "${env.repoName}"
  }
  steps{
      script
      {
        try {
          sh 'curl -I "https://api.callmebot.com/whatsapp.php?phone=+5491132617901&text=Termino+la+ejecucion+de+{repoName}&apikey=439147"'
        }
        catch(Exception e){
              print(e.getMessage())
          }
      }
    }
}
