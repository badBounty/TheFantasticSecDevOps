 def runStage(){
    sshagent(['ssh-key']) {
        def projname = env.JOB_NAME
        sh 'ssh-keygen -f "/var/jenkins_home/.ssh/known_hosts" -R [192.168.0.23]:44022'
        sh "ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 rm -rf /home/${projname}/"
        sh 'scp -P 44022 -o StrictHostKeyChecking=no -r $(pwd) root@192.168.0.23:/home '
        sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package Puma.Security.Rules -v 2.3.0 \\\\\\;"""
        sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet add {} package SecurityCodeScan.VS2017 -v 2.3.0 \\\\\\;"""
        sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet clean {} \\\\\\;"""
        sh """ssh -p 44022 -o StrictHostKeyChecking=no root@192.168.0.23 find  /home/${projname} -name \\"*.csproj\\" -exec /usr/share/dotnet/dotnet build \\"/flp:logfile=/home/${projname}/build.log\\;verbosity=normal\\" \\"/flp1:logfile=/home/${projname}/errors.log\\;errorsonly\\" \\"/flp2:logfile=/home/${projname}/warnings.log\\;warningsonly\\" {} \\\\\\;"""
        sh "scp -P 44022 -o StrictHostKeyChecking=no root@192.168.0.23:/home/${projname}/warnings.log ./warnings.log"
        sh "grep -E 'warning SCS|warning SEC' warnings.log  > secissues.log"
        sh "cat secissues.log"
    }
 }