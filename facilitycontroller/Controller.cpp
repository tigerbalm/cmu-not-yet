// 
// 
// 

#include "Controller.h"
#include "SureParkPinHeader.h"

Controller::Controller(MsgQueClient & client)
{
	msgQueClient = &client;
}

void Controller::setup()
{	
	waitingState = new WatingState(msgQueClient, this);
	parkingState = new ParkingState(msgQueClient, this);
	leavingState = new LeavingState(msgQueClient, this);

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
	entryGateCarDetector->loop();
	exitGateCarDetector->loop();
	slotStatusChangeDetector->loop();

	current->loop();
}

void Controller::receiveMessage(Command *command)
{
	Serial.println("Controller::receiveMessage: ");
	Serial.println(command->toString());

	current->onMessageReceived(command);
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
