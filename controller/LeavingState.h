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
#include "NetworkManager.h"

class LeavingState : public State {
private:
	StateChangeListener *stateChangeListener;
	NetworkManager *networkManager;
	int mode;

public:
	void loop();

	LeavingState(StateChangeListener *listener, NetworkManager *manager);

	void onMessageReceived(String message);
	void carDetectedOnEntry(int status);
	void carDetectedOnExit(int status);

	void onSlotOccupied(int slotNum);
	void onSlotEmptified(int slotNum);
};

#endif

