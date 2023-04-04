/*def runStage(notifier)
{
  try
  {
    sh 'curl -I "https://api.callmebot.com/whatsapp.php?phone=+5491132617901&text=Termino+la+ejecucion+de+{repoName}&apikey=439147"'
  }
  catch(Exeption e)
  {
    print(e.getMessage())
  }
}
return this
*/
stage('SAST-Whatsapp-Notification')
{
  step{
      script
      {
        try {
          sh 'curl -I "https://api.callmebot.com/whatsapp.php?phone=+5491132617901&text=Termino+la+ejecucion+de+${env.repoName}&apikey=439147"'
        }
        catch(Exception e){
              print(e.getMessage())
          }
      }
    }
}
