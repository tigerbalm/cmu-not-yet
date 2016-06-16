// ParkingState.h

#ifndef _PARKINGSTATE_h
#define _PARKINGSTATE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "State.h"
#include "StateChangeListener.h"
#include "NetworkManager.h"

class ParkingState : public State {
private:
	StateChangeListener *stateChangeListener;
	NetworkManager *networkManager;

	int mode;
public:
	void loop();
	void waitingNumberInput();
	void verifyReservation(int number);	

	ParkingState(StateChangeListener *listener, NetworkManager *manager);

	void onMessageReceived(String message);
	void carDetectedOnEntry(int status);
	void carDetectedOnExit(int status);

	void onSlotOccupied(int slotNum);
	void onSlotEmptified(int slotNum);
};

#endif

