def Init(def strategySetup){
    strategy = strategySetup
}

def runStage(def channel, def color, def message){
	strategy.sendMessage(channel, color, message)
}