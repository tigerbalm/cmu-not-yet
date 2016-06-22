// Controller.h

#ifndef _CONTROLLER_h
#define _CONTROLLER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "State.h"
#include "NoServerConnectState.h"
#include "WatingState.h"
#include "ParkingState.h"
#include "LeavingState.h"
#include "GateCarDetector.h"
#include "SlotStatusChangeListener.h"
#include "SlotStatusChangeDetector.h"
#include "MsgQueClient.h"
#include "MsgQueClientStatusListener.h"
#include "Command.h"

class Controller : public CarDetectedListener, 
	public SlotStatusChangeListener, public MsgQueClientStatusListener
{
 protected:
	 State *currentState;

	 State *noServerConnectState;
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
	 State* getCurrentState();
	 State * getWaitingState();
	 State * getParkingState();
	 State * getLeavingState();
	 State * getNoServerConnectState();

	 void loop();

	 void receiveMessage(Command *command);

	 /* from CarDetectedListener */
	 void onCarChangeDetected(int gatePin, int status);

	 /* from SlotStatusChangeListener */
	 void onSlotChange(int slotNumber, int status);

	 /* from MsgQueClientSatusListener */
	 void onMsgQueStatusChange(int status);
};
#endif

