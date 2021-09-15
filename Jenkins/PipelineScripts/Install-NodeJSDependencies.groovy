def runStage(notifier)
{
	try 
	{
		notifier.sendMessage('','good','Stage: "Install-Dependencies": INIT')
		sh 'npm install --force --legacy-peer-deps'
		notifier.sendMessage('','good','Stage: "Install-Dependencies": SUCCESS')
	} 
	catch(Exception e)
	{
		notifier.sendMessage('','danger','Stage: "Install-Dependencies": FAILURE')
		
		currentBuild.result = 'FAILURE'
		print('Stage: "Install-Dependencies": FAILURE')
		print(e.printStackTrace())
	} 
}
return this
