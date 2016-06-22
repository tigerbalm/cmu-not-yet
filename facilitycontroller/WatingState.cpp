// 
// 
// 

#include "WatingState.h"
#include "CarDetectedListener.h"
#include "Controller.h"

void WatingState::loop()
{
	Serial.println("WatingState::loop()");
}

void WatingState::onMessageReceived(Command *command)
{
	Serial.println("WatingState::onMessageReceived: ");
}

void WatingState::carDetectedOnEntry(int status)
{
	Serial.println("WatingState::carDetectedOnEntry()");

	if (status != CAR_DETECTED)
	{
		return;
	}

	controller->setState(controller->getParkingState());
}

void WatingState::carDetectedOnExit(int status)
{
	if (status != CAR_DETECTED)
	{
		return;
	}

	Serial.println("WatingState::carDetectedOnExit()");
	Serial.println("Error: if you see this message, something went wrong... this status cannot be entered.");
		
	// todo: tell dave...
}

void WatingState::onSlotOccupied(int slotNum)
{
	Serial.print("WatingState::onSlotOccupied() - ");
	Serial.println(slotNum);
}

void WatingState::onSlotEmptified(int slotNum)
{
	Serial.print("WatingState::onSlotEmptified() - ");
	Serial.println(slotNum);

	LeavingState *leavingState = (LeavingState *)controller->getLeavingState();
	leavingState->setSlotNum(slotNum);

	controller->setState(leavingState);
}

void WatingState::onMsgQueClientConnected()
{
}

void WatingState::onMsgQueClientDisconnected()
{
}
