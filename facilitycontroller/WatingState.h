// WatingState.h

#ifndef _WATINGSTATE_h
#define _WATINGSTATE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "State.h"
#include "StateChangeListener.h"
#include "NetworkManager.h"

class WatingState :public State {	
	StateChangeListener *stateChangeListener;
	NetworkManager *networkManager;
public:
	void loop();

	WatingState(StateChangeListener *listener, NetworkManager *manager);

	void onMessageReceived(String message);
	void carDetectedOnEntry(int status);
	void carDetectedOnExit(int status);

	void onSlotOccupied(int slotNum);
	void onSlotEmptified(int slotNum);
};
#endif

