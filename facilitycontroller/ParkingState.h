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
#include "MsgQueClient.h"
#include "Command.h"

class ParkingState : public State {
private:
	int bookingNo;
	int assignedSlot;
	int mode;
public:
	void loop();
	void waitingNumberInput();
	void verifyReservation(int number);	

	ParkingState(MsgQueClient *_client, Controller *_controller);
		
	void exit();

	void enter();

	void onMessageReceived(Command *command);

	void setMode(int mode);

	void carDetectedOnEntry(int status);
	void carDetectedOnExit(int status);

	void onSlotOccupied(int slotNum);
	void onSlotEmptified(int slotNum);

	void onMsgQueClientConnected();
	void onMsgQueClientDisconnected();
};

#endif

