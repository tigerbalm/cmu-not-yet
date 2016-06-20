// 
// 
// 

#include "WatingState.h"
#include "CarDetectedListener.h"
#include "CommandReportSlotStatus.h"
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

	// send server
	CommandReportSlotStatus cmdStatus(mqClient, slotNum, 1);
	cmdStatus.send();
}

void WatingState::onSlotEmptified(int slotNum)
{
	Serial.print("WatingState::onSlotEmptified() - ");
	Serial.println(slotNum);

	CommandReportSlotStatus cmdStatus(mqClient, slotNum, 0);
	cmdStatus.send();

	LeavingState *leavingState = (LeavingState *)controller->getLeavingState();
	leavingState->setSlotNum(slotNum);

	controller->setState(leavingState);	
}