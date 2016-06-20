// 
// 
// 

#include "ParkingState.h"
#include "EntryGateHelper.h"
#include "CarDetectedListener.h"
#include "CommandFactory.h"
#include "CmdVerifyBookingReq.h"
#include "CmdVerifyBookingResp.h"
#include "CmdCarParkedNoti.h"
#include "Controller.h"
#include "SlotLedController.h"

#define MODE_WAITING_NUMBER		1
#define MODE_WAITING_CONFIRM	2
#define MODE_WAITING_PLACING	3

ParkingState::ParkingState(MsgQueClient *_client, Controller *_controller)
	: State(_client, _controller)
{
	mode = MODE_WAITING_NUMBER;
}

void ParkingState::exit()
{
	Serial.println("ParkingState::exit()");

	mode = MODE_WAITING_CONFIRM;
	bookingNo = -1;
	assignedSlot = -1;	
}

void ParkingState::onMessageReceived(Command *command)
{
	Serial.println("ParkingState::onMessageReceived: ");

	if (mode == MODE_WAITING_CONFIRM)
	{
		CmdVerifyBookingResp *response = (CmdVerifyBookingResp *)command;

		if (response->isSuccess())
		{
			assignedSlot = response->getSlotNumber();

			Serial.print("confirm_reservation success : ");
			Serial.println(assignedSlot);
			
			SlotLedController::getInstance()->on(assignedSlot);

			EntryGateHelper::open();
			EntryGateHelper::ledOn();

			mode = MODE_WAITING_PLACING;
		}
		else
		{
			Serial.print("confirm_reservation fail : ");
			Serial.println(response->getFailCause());

			mode = MODE_WAITING_NUMBER;
		}
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
			/*
		{
			String msg = "{ 'success':1, 'slot_no':3 }";
			onMessageReceived(msg);
		}
		*/
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
		bookingNo = reservation;
		verifyReservation(reservation);		
	}
}

void ParkingState::verifyReservation(int number)
{
	// send to server with number
	CmdVerifyBookingReq* verifyRequest = (CmdVerifyBookingReq*)CommandFactory::getInstance()->createCommand(CMD_HINT_CONFIRM_RESERVATION_REQ);
	verifyRequest->setReservationNumber(number);
	verifyRequest->send(mqClient);

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

		controller->setState(controller->getWaitingState());		
	}
}

void ParkingState::carDetectedOnExit(int status)
{
	Serial.print("ParkingState::carDetectedOnExit() - ");
	Serial.println(status);

	// notify to dave
}

void ParkingState::onSlotOccupied(int slotNum)
{
	Serial.print("ParkingState::onSlotOccupied() - ");
	Serial.println(slotNum);

	if (mode == MODE_WAITING_PLACING) {
		CmdCarParkedNoti* noti = (CmdCarParkedNoti*)CommandFactory::getInstance()->createCommand(CMD_HINT_CAR_PARKED_NOTIFY);
		noti->setData(bookingNo, slotNum);
		noti->send(mqClient);
		
		SlotLedController::getInstance()->off(assignedSlot);

		EntryGateHelper::ledOff();
		EntryGateHelper::close();
		
		controller->setState(controller->getWaitingState());
	}
}

void ParkingState::onSlotEmptified(int slotNum)
{
	Serial.print("ParkingState::onSlotEmptified() - ");
	Serial.println(slotNum);

	// this case is error.... 
	// while parking one car, another car can not move or enter or leave...

	// notify to dave
}