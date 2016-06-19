// State.h

#ifndef _STATE_h
#define _STATE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Command.h"
#include "StateChangeListener.h"
#include "MsgQueClient.h"

class State
{
protected:
	StateChangeListener *stateChangeListener;
	MsgQueClient *mqClient;

public:
	State(MsgQueClient *_client, StateChangeListener *_listener) { 
		mqClient = _client;  
		stateChangeListener = _listener; 
	}

	virtual void loop() = 0;
	virtual void onMessageReceived(Command *command) = 0;
	virtual void carDetectedOnEntry(int status) = 0;
	virtual void carDetectedOnExit(int status) = 0;
	virtual void onSlotOccupied(int slotNum) = 0;
	virtual void onSlotEmptified(int slotNum) = 0;
};
#endif

