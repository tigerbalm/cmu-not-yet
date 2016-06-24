// 
// 
// 

#include "ParkingState.h"
#include "GateHelper.h"
#include "CarDetectedListener.h"
#include "CommandFactory.h"
#include "CmdVerifyBookingReq.h"
#include "CmdVerifyBookingResp.h"
#include "CmdCarParkedNoti.h"
#include "Controller.h"
#include "SlotLedController.h"
#include "CmdReceiveBookingNumber.h"
#include "CmdExceptionNoti.h"

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

	// deactive kiosk
	Serial.println("#$control##deactivate_kiosk$#");
}

void ParkingState::enter()
{
	setMode(MODE_WAITING_NUMBER);
	
	GateHelper::entryGate()->ledGreen(true);

	bookingNo = -1;
	assignedSlot = -1;
}

void ParkingState::onMessageReceived(Command *command)
{
	Serial.println("ParkingState::onMessageReceived: ");

	if (mode == MODE_WAITING_CONFIRM)
	{
		if (command->getHint() != CMD_HINT_CONFIRM_RESERVATION_RESP) {
			Serial.print("ParkingState::casting error - ");
			Serial.println("CMD_HINT_CONFIRM_RESERVATION_RESP");
			return;
		}

		CmdVerifyBookingResp *response = (CmdVerifyBookingResp *)command;

		if (response->isSuccess())
		{
			assignedSlot = response->getSlotNumber();

			Serial.print("confirm_reservation success : ");
			Serial.println(assignedSlot);

			SlotLedController::getInstance()->blinkOn(assignedSlot);

			GateHelper::entryGate()->openDoor();

			setMode(MODE_WAITING_PLACING);			
		}
		else
		{
			Serial.print("confirm_reservation fail : ");
			Serial.println(response->getFailCause());

			sendException(response->getFailCause());

			String message = "#$error##";
			message += "Your confirmation no. is incorrect.<br>Pleae try again or<br><b>CALL DAVE!!!</b>";
			message += "<br><br>";
			message += response->getFailCause();
			message += "$#";
			Serial.println(message);
			
			setMode(MODE_WAITING_NUMBER);
		}		
	}
	else if (mode == MODE_WAITING_NUMBER)
	{
		if (command->getHint() != CMD_HINT_RECEIVE_BOOK_NO) {
			Serial.print("ParkingState::casting error - ");
			Serial.println("CMD_HINT_RECEIVE_BOOK_NO");			
			return;
		}

		CmdReceiveBookingNumber *cmd = (CmdReceiveBookingNumber *)command;
		bookingNo = cmd->getBookingNo();;
		verifyReservation(bookingNo);
	}
}

void ParkingState::sendException(String exception)
{
	CmdExceptionNoti * exceptionCmd = (CmdExceptionNoti*)CommandFactory::getInstance()->createCommand(CMD_HINT_EXCEPTION_NOTIFY);
	exceptionCmd->setMessage(exception);
	exceptionCmd->send(mqClient);
}

void ParkingState::setMode(int _mode)
{
	switch (_mode) {
		case MODE_WAITING_NUMBER:
			Serial.println("#$control##activate_kiosk$#");
			break;
		case MODE_WAITING_CONFIRM:
			Serial.println("#$control##deactivate_kiosk$#");
			break;
		case MODE_WAITING_PLACING:
			startPlacingMode = millis();
			break;
	}

	mode = _mode;
}

void ParkingState::loop()
{
	Serial.print("ParkingState::loop() - ");
	Serial.println(mode);
	
	switch (mode) {
		case MODE_WAITING_NUMBER :
			//Serial.println("#$control##activate_kiosk$#");
			break;
		case MODE_WAITING_CONFIRM :	
			//Serial.println("#$control##deactivate_kiosk$#");
			break;
		case MODE_WAITING_PLACING :	
			{
				long now = millis();
				if (now - startPlacingMode > 30*1000)
				{
					startPlacingMode = now;
					sendException("Reserved car is not parked!!");
				}
			}
			break;
	}
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

	setMode(MODE_WAITING_CONFIRM);	
}

void ParkingState::carDetectedOnEntry(int status)
{
	Serial.print("ParkingState::carDetectedOnEntry() - ");
	Serial.println(status);

	if (status == CAR_DETECTED) {
		return;
	}

	if (mode != MODE_WAITING_PLACING)
	{
		GateHelper::entryGate()->ledGreen(false);
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
		
		SlotLedController::getInstance()->blinkOff(assignedSlot);

		GateHelper::entryGate()->closeDoor();
		
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

void ParkingState::onMsgQueClientConnected()
{
}

void ParkingState::onMsgQueClientDisconnected()
{
}
