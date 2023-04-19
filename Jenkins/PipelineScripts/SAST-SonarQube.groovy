def runStage() {
    try {
        withCredentials([usernamePassword(credentialsId: 'sonar-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {       
            sshagent(['ssh-key-SAST-image']) {
                sh "ssh -p ${env.SAST_SERVER_SSH_PORT} -o StrictHostKeyChecking=no root@${env.SAST_SERVER_IP} /home/sonarscanner/bin/sonar-scanner -Dsonar.login=${USERNAME} -Dsonar.password=${PASSWORD} -Dsonar.projectKey=${env.REPO_TO_SCAN_NAME} -Dsonar.projectBaseDir=/home/${env.REPO_TO_SCAN_NAME} -Dsonar.host.url=http://${env.SAST_SERVER_IP}:${env.SONAR_PORT}"
            }
            print ('Stage "SAST-SonarQube: SUCCESS"')
        }
    }
    catch(Exception e) {
        currentBuild.result = 'FAILURE'
        print('Stage "SAST-SonarQube": FAILURE')
        print(e.printStackTrace())
    }
}
return this
