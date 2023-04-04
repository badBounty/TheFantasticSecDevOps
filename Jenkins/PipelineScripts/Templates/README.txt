Revisar si es necesario dejar "disableConcurrentBuilds()" (no permite crear una cola de pipelines)
To enable Whatsapp notification add the following stage to the desired pipeline (your phone number must be registered in callmebot API):

stage('SAST-Whatsapp-Notification') {
            steps {
                script {
                    try {
                        sh "curl -Ik 'https://api.callmebot.com/whatsapp.php?phone=+{REGISTERED-PHONE-NUMBER}&text=Termino+la+ejecucion+de+${env.repoName}&apikey={API-KEY}'"
                    }
                    catch(Exception e) {
                        print(e.getMessage())
                    }
                }
            }
        }
