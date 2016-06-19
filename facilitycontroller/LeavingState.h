// LeavingState.h

#ifndef _LEAVINGSTATE_h
#define _LEAVINGSTATE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "State.h"
#include "StateChangeListener.h"
#include "MsgQueClient.h"
#include "Command.h"

class LeavingState : public State {
private:
	int mode;

public:
	void loop();

	LeavingState(MsgQueClient *_client, StateChangeListener *_listener) : State(_client, _listener) {};
	
	void onMessageReceived(Command *command);

	void carDetectedOnEntry(int status);
	void carDetectedOnExit(int status);

	void onSlotOccupied(int slotNum);
	void onSlotEmptified(int slotNum);
};

#endif

