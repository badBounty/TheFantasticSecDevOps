strategy = null

def Init(def strategySetup)
{
    strategy = strategySetup
}

def sendMessage(def channel, def color, def message)
{
	strategy.sendMessage(channel, color, message)
}
return this