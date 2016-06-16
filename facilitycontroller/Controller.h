// Controller.h

#ifndef _CONTROLLER_h
#define _CONTROLLER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "NetworkManager.h"
#include "NetworkManagerListener.h"
#include "State.h"
#include "WatingState.h"
#include "ParkingState.h"
#include "LeavingState.h"
#include "StateChangeListener.h"
#include "GateCarDetector.h"
#include "SlotStatusChangeListener.h"
#include "SlotStatusChangeDetector.h"

class Controller : public StateChangeListener, public NetworkManagerListener, public CarDetectedListener, public SlotStatusChangeListener
{
 protected:
	 State *current;

	 State *waitingState;
	 State *parkingState;
	 State *leavingState;
	 
	 NetworkManager *networkManager;
	 GateCarDetector *entryGateCarDetector;
	 GateCarDetector *exitGateCarDetector;
	 SlotStatusChangeDetector *slotStatusChangeDetector;

 public:
	 Controller();
	 void setup();
	 void setState(State* state);
	 State* getState();
	 void loop();

	 /* from StateChangeListener */
	 void onStateChanged(int nextState);

	 /* from NetworkManagerListener */
	 void onMessageReceived(String message);

	 /* from CarDetectedListener */
	 void onCarChangeDetected(int gatePin, int status);

	 /* from SlotStatusChangeListener */
	 void onSlotChange(int slotNumber, int status);
};
#endif

