// 
// 
// 

#include "WatingState.h"
#include "CarDetectedListener.h"

WatingState::WatingState(StateChangeListener *listener, NetworkManager *manager) {
	stateChangeListener = listener;
	networkManager = manager;
}

void WatingState::loop()
{
	Serial.println("WatingState::loop()");
}

void WatingState::onMessageReceived(String message)
{
	Serial.print("WatingState::onMessageReceived: ");
	Serial.println(message);
}

void WatingState::carDetectedOnEntry(int status)
{
	Serial.println("WatingState::carDetectedOnEntry()");

	if (status != CAR_DETECTED)
	{
		return;
	}

	if (stateChangeListener != NULL)
	{
		stateChangeListener->onStateChanged(STATE_PARKING);
	}
}

void WatingState::carDetectedOnExit(int status)
{
	if (status != CAR_DETECTED)
	{
		return;
	}

	Serial.println("WatingState::carDetectedOnExit()");
	Serial.println("Error: if you see this message, something went wrong... this status cannot be entered.");

	if (stateChangeListener != NULL)
	{
		stateChangeListener->onStateChanged(STATE_LEAVING);
	}
}

void WatingState::onSlotOccupied(int slotNum)
{
	Serial.print("WatingState::onSlotOccupied() - ");
	Serial.println(slotNum);

	// send server
}

void WatingState::onSlotEmptified(int slotNum)
{
	Serial.print("WatingState::onSlotEmptified() - ");
	Serial.println(slotNum);
	
	// todo: leaving ���ٰ� slotnum �� ���� ��� �� 
	//		-> listner���� controller �� ������ �ٴϴ� ������� ����
	if (stateChangeListener != NULL)
	{
		stateChangeListener->onStateChanged(STATE_LEAVING);

		// LeavingState *state = controller->getLeavingState();
		// state->setSlotNum(slotnum);
		// controller->setState(state);

		// Leaving ������ slot numn �� ������ �ְ� -> car detection �Ǹ� ���� ��û -> �Ϸ� �Ǹ� ������ ��.
	}
}