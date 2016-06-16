// 
// 
// 

#include "ParkingState.h"
#include "EntryGateHelper.h"
#include "CarDetectedListener.h"

#define MODE_WAITING_NUMBER		1
#define MODE_WAITING_CONFIRM	2
#define MODE_WAITING_PLACING	3

ParkingState::ParkingState(StateChangeListener * listener, NetworkManager *manager)
{
	stateChangeListener = listener;
	networkManager = manager;

	mode = MODE_WAITING_NUMBER;
}

void ParkingState::onMessageReceived(String message)
{
	Serial.print("ParkingState::onMessageReceived: ");
	Serial.println(message);

	if (mode == MODE_WAITING_CONFIRM)
	{
		// door open??
		EntryGateHelper::open();
		EntryGateHelper::ledOn();

		mode = MODE_WAITING_PLACING;
	}
}

void ParkingState::loop()
{
	Serial.print("ParkingState::loop() - ");
	Serial.println(mode);
	
	switch (mode) {
		case MODE_WAITING_NUMBER :
			waitingNumberInput();
			break;
		case MODE_WAITING_CONFIRM :			
			break;
		case MODE_WAITING_PLACING :			
			break;
	}
	// number input
	// send to server
	// 
}

void ParkingState::waitingNumberInput()
{
	Serial.setTimeout(1000);
	int reservation = Serial.parseInt();

	if (reservation != 0)	// success
	{
		verifyReservation(reservation);		
	}
}

void ParkingState::verifyReservation(int number)
{
	// send to server with number
	networkManager->send("verify", number);
	mode = MODE_WAITING_CONFIRM;
}

void ParkingState::carDetectedOnEntry(int status)
{
	Serial.print("ParkingState::carDetectedOnEntry() - ");
	Serial.println(status);

	if (status == CAR_DETECTED) {
		return;
	}

	if (mode == MODE_WAITING_NUMBER || mode == MODE_WAITING_CONFIRM) {
		Serial.println("Car is disappeared. Change to Waiting state.");
		if (stateChangeListener != NULL)
		{
			stateChangeListener->onStateChanged(STATE_WAITING);
		}
	}
}

void ParkingState::carDetectedOnExit(int status)
{
	Serial.print("ParkingState::carDetectedOnExit() - ");
	Serial.println(status);
}

void ParkingState::onSlotOccupied(int slotNum)
{
	Serial.print("ParkingState::onSlotOccupied() - ");
	Serial.println(slotNum);

	if (mode == MODE_WAITING_PLACING) {
		// TODO: 
		// 1. server �� slot ���� ������.
		// 2. Ư���� ����̹Ƿ� ������ ���� ��ȣ�� ���� ���� �� ���� �ʿ�

		EntryGateHelper::ledOff();
		EntryGateHelper::close();

		if (stateChangeListener != NULL)
		{
			stateChangeListener->onStateChanged(STATE_WAITING);
		}
	}
}

void ParkingState::onSlotEmptified(int slotNum)
{
	Serial.print("ParkingState::onSlotEmptified() - ");
	Serial.println(slotNum);

	// this case is error.... 
	// while parking one car, another car can not move or enter or leave...
}