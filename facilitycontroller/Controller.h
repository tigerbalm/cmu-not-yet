// Controller.h

#ifndef _CONTROLLER_h
#define _CONTROLLER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "State.h"
#include "WatingState.h"
#include "ParkingState.h"
#include "LeavingState.h"
#include "StateChangeListener.h"
#include "GateCarDetector.h"
#include "SlotStatusChangeListener.h"
#include "SlotStatusChangeDetector.h"
#include "MsgQueClient.h"
#include "Command.h"

class Controller : public StateChangeListener, public CarDetectedListener, public SlotStatusChangeListener
{
 protected:
	 State *current;

	 State *waitingState;
	 State *parkingState;
	 State *leavingState;
	 
	 MsgQueClient *msgQueClient;

	 GateCarDetector *entryGateCarDetector;
	 GateCarDetector *exitGateCarDetector;
	 SlotStatusChangeDetector *slotStatusChangeDetector;

 public:
	 Controller(MsgQueClient &client);
	 void setup();
	 void setState(State* state);
	 State* getState();
	 void loop();

	 void receiveMessage(Command *command);

	 /* from StateChangeListener */
	 void onStateChanged(int nextState);

	 /* from CarDetectedListener */
	 void onCarChangeDetected(int gatePin, int status);

	 /* from SlotStatusChangeListener */
	 void onSlotChange(int slotNumber, int status);
};
#endif

