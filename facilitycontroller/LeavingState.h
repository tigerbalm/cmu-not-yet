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
	int slot;

	long startOpenGateMode;
public:
	LeavingState(MsgQueClient *_client, Controller *_controller) : State(_client, _controller) {};
	
	void enter();
	void exit();

	void setMode(int _mode);

	void loop();
	
	void setSlotNum(int _slot);

	void onMessageReceived(Command *command);

	void sendException(String exception);

	void carDetectedOnEntry(int status);
	void carDetectedOnExit(int status);

	void onSlotOccupied(int slotNum);
	void onSlotEmptified(int slotNum);

	void onMsgQueClientConnected();
	void onMsgQueClientDisconnected();
};

#endif

