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
	Serial.println("Controller::setup() - start");

	waitingState = new WatingState(msgQueClient, this);
	parkingState = new ParkingState(msgQueClient, this);
	leavingState = new LeavingState(msgQueClient, this);
	noServerConnectState = new NoServerConnectState(msgQueClient, this);

	if (msgQueClient->connected())
	{
		setState((State*)waitingState);
	}
	else
	{
		setState((State*)noServerConnectState);
	}	

	entryGateCarDetector = new GateCarDetector(this, ENTRY_BEAM_RECEIVER);
	exitGateCarDetector = new GateCarDetector(this, EXIT_BEAM_RECEIVER);

	slotStatusChangeDetector = new SlotStatusChangeDetector(this);

	Serial.println("Controller::setup() - end");
}

void Controller::setState(State* newState)
{
	if (currentState != NULL)
	{ 
		currentState->exit();
	}	

	currentState = newState;
	
	currentState->enter();
}

State* Controller::getCurrentState()
{
	return currentState;
}

State* Controller::getWaitingState()
{
	return waitingState;
}

State* Controller::getParkingState()
{
	return parkingState;
}

State* Controller::getLeavingState()
{
	return leavingState;
}

State * Controller::getNoServerConnectState()
{
	return noServerConnectState;
}

void Controller::loop()
{
	//Serial.println("Controller::loop()");

	if (entryGateCarDetector == NULL) {
		Serial.println("entryGate null");
		delay(1000);
	}

	entryGateCarDetector->loop();
	exitGateCarDetector->loop();
	slotStatusChangeDetector->loop();

	currentState->loop();
}

void Controller::receiveMessage(Command *command)
{
	Serial.println("Controller::receiveMessage: ");
	Serial.println(command->toString());

	currentState->onMessageReceived(command);
}

void Controller::onCarChangeDetected(int gatePin, int status)
{
	if (gatePin == ENTRY_BEAM_RECEIVER)
	{
		currentState->carDetectedOnEntry(status);
	}
	else
	{
		currentState->carDetectedOnExit(status);
	}
}

void Controller::onSlotChange(int slotNumber, int status)
{
	if (status == SLOT_OCCUPIED)
	{
		currentState->onSlotOccupied(slotNumber);
	}
	else
	{
		currentState->onSlotEmptified(slotNumber);
	}
}

void Controller::onMsgQueStatusChange(int status)
{
	if (currentState == NULL) 
	{
		return;
	}

	if (status == MSG_QUE_CLIENT_STATUS_CONNECTED)
	{
		Serial.println("MsgQueClinet connected!!");
		currentState->onMsgQueClientConnected();
	}
	else
	{
		Serial.println("MsgQueClinet disconnected!!");
		currentState->onMsgQueClientDisconnected();
	}
}
