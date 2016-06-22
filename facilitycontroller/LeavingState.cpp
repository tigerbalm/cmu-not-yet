// 
// 
// 

#include "LeavingState.h"
#include "ExitGateHelper.h"
#include "CarDetectedListener.h"
#include "Controller.h"
#include "CmdPaymentResp.h"
#include "CmdPaymentReq.h"
#include "SlotLedController.h"
#include "CommandFactory.h"

// LEAVING �� ó�� �����ϸ� ���� EXIT GATE �� �����ϱ� ��ٸ��� ����
// �����ϸ�, ���� ��û
// ���� �Ϸ� �Ǹ� open gate
// exit gate ���� ���� �������� close gate
// wating ���� ��ȯ
#define MODE_WAITING_CAR_AT_EXIT		3000
#define	MODE_WAITING_PAYMENT_RESP		3001
#define	MODE_OPEN_GATE					3002

void LeavingState::enter()
{
	mode = MODE_WAITING_CAR_AT_EXIT;

	SlotLedController::getInstance()->blinkOn(slot);
}

void LeavingState::exit()
{
	mode = MODE_WAITING_CAR_AT_EXIT;

	SlotLedController::getInstance()->blinkOff(slot);
}

void LeavingState::loop()
{
	Serial.print("LeavingState - ");
	Serial.println(mode);

	switch (mode) {
	case MODE_WAITING_CAR_AT_EXIT :
		break;
	case MODE_WAITING_PAYMENT_RESP :		
		break;
	case MODE_OPEN_GATE :
		break;
	}
}

void LeavingState::setSlotNum(int _slot)
{
	slot = _slot;
}

void LeavingState::onMessageReceived(Command *command)
{
	Serial.println("LeavingState::onMessageReceived: ");

	if (mode == MODE_WAITING_PAYMENT_RESP)
	{		
		if (command->getHint() != CMD_HINT_PAYMENT_RESP) {
			Serial.print("ParkingState::casting error - ");
			Serial.println("CMD_HINT_PAYMENT_RESP");
			return;
		}
		
		CmdPaymentResp *resp = (CmdPaymentResp *)command;
		
		if (resp->isSuccess())
		{
			ExitGateHelper::ledOn();
			ExitGateHelper::open();

			mode = MODE_OPEN_GATE;
		}
		else
		{
			// notify to dave
			
			// slot led blink...
		}
	}
}

void LeavingState::carDetectedOnEntry(int status)
{
	Serial.print("LeavingState::carDetectedOnEntry() - ");
	Serial.println(status);

	// notify dave
}

void LeavingState::carDetectedOnExit(int status)
{
	Serial.print("LeavingState::carDetectedOnExit() - ");
	Serial.println(status);
	
	if (status == CAR_DETECTED)
	{
		if (mode == MODE_WAITING_CAR_AT_EXIT)
		{
			// send req to server
			CmdPaymentReq* payRequest = (CmdPaymentReq *)CommandFactory::getInstance()->createCommand(CMD_HINT_PAYMENT_REQ);
			payRequest->setSlot(slot);
			payRequest->send(mqClient);

			mode = MODE_WAITING_PAYMENT_RESP;
		}
	}
	else
	{
		if (mode == MODE_OPEN_GATE) {
			ExitGateHelper::ledOff();
			ExitGateHelper::close();

			controller->setState(controller->getWaitingState());
		}
	}
}

void LeavingState::onSlotOccupied(int _slotNum)
{
	Serial.print("LeavingState::onSlotOccupied() - ");
	Serial.println(_slotNum);
		
	// ��� �ó�����
	if (mode == MODE_WAITING_CAR_AT_EXIT 
				&& slot == _slotNum) {
		Serial.println("Leaving is canceled...");

		controller->setState(controller->getWaitingState());

		return;
	}

	// notify to dave
}

void LeavingState::onSlotEmptified(int _slotNum)
{
	Serial.print("LeavingState::onSlotEmptified() - ");
	Serial.println(_slotNum);

	// notify to dave
}