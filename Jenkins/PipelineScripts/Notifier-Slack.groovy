def sendMessage(def channel, def color, def message)
{
	if(channel.isEmpty())
	{
		slackSend color: color , message: message, channel: env.SlackChannel
	}
	else
	{
		slackSend color: color , message: message, channel: channel
	}
}
return this