def runStage() {
    try {
        sshagent(['ssh-key-SAST-server']) {
            sh "ssh -o StrictHostKeyChecking=no ${env.SAST_SERVER_USERNAME}@${env.SAST_SERVER_IP} docker container rm -v -f ${REPO_TO_SCAN_NAME}"
            print('Stage "SAST-Destroy": SUCCESS')
        }
    }
    catch(Exception e) {
		currentBuild.result = 'FAILURE'
		print('Stage: "SAST-Destroy": FAILURE')
        print(e.printStackTrace())
    }
}
return this
