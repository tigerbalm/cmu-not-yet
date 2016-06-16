// 
// 
// 

#include "Controller.h"
#include "SureParkPinHeader.h"

Controller::Controller()
{	
}

void Controller::setup()
{
	networkManager = new NetworkManager(this);

	waitingState = new WatingState(this, networkManager);
	parkingState = new ParkingState(this, networkManager);
	leavingState = new LeavingState(this, networkManager);

	setState((State*)waitingState);

	entryGateCarDetector = new GateCarDetector(this, ENTRY_BEAM_RECEIVER);
	exitGateCarDetector = new GateCarDetector(this, EXIT_BEAM_RECEIVER);

	slotStatusChangeDetector = new SlotStatusChangeDetector(this);
}

void Controller::setState(State* state)
{
	current = state;
}

State* Controller::getState()
{
	return current;
}

void Controller::loop()
{
	//Serial.println("Controller::loop()");

	networkManager->loop();
	entryGateCarDetector->loop();
	exitGateCarDetector->loop();
	slotStatusChangeDetector->loop();

	current->loop();
}

void Controller::onStateChanged(int nextState)
{
	Serial.println("State changed");

	switch (nextState)
	{
		case STATE_PARKING :
			setState(parkingState);
			break;
		case STATE_WAITING :
			setState(waitingState);
			break;
		case STATE_LEAVING :
			setState(leavingState);
			break;
		default:
			// todo throw run-time exception... how????
			break;
	}
}

void Controller::onMessageReceived(String message)
{
	// somthing like that
	// current->messageReceived();
	current->onMessageReceived(message);
}

void Controller::onCarChangeDetected(int gatePin, int status)
{
	if (gatePin == ENTRY_BEAM_RECEIVER)
	{
		current->carDetectedOnEntry(status);
	}
	else
	{
		current->carDetectedOnExit(status);
	}
}

void Controller::onSlotChange(int slotNumber, int status)
{
	if (status == SLOT_OCCUPIED)
	{
		current->onSlotOccupied(slotNumber);
	}
	else
	{
		current->onSlotEmptified(slotNumber);
	}
}
