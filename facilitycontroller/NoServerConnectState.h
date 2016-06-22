// NoServerConnectState.h

#ifndef _NOSERVERCONNECTSTATE_h
#define _NOSERVERCONNECTSTATE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "State.h"
#include "MsgQueClient.h"
#include "Command.h"

class NoServerConnectState : public State
{
	void sendErrorToKiosk();

public:
	NoServerConnectState(MsgQueClient *_client, Controller *_controller) : State(_client, _controller) {};

	void loop();	
	void onMessageReceived(Command *command);

	void carDetectedOnEntry(int status);
	void carDetectedOnExit(int status);

	void onSlotOccupied(int slotNum);
	void onSlotEmptified(int slotNum);

	void onMsgQueClientConnected();
	void onMsgQueClientDisconnected();
};
#endif

