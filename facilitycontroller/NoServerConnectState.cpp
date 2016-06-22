// 
// 
// 

#include "NoServerConnectState.h"
#include "CmdReceiveBookingNumber.h"
#include "Controller.h"

void NoServerConnectState::loop()
{
	Serial.println("NoServerConnectState");
}

void NoServerConnectState::sendErrorToKiosk()
{
	Serial.println("#$error##Network error<br>Please call dave!!$#");
}

void NoServerConnectState::onMessageReceived(Command * command)
{
	if (command->getHint() != CMD_HINT_RECEIVE_BOOK_NO) {
		Serial.print("ParkingState::casting error - ");
		Serial.println("CMD_HINT_RECEIVE_BOOK_NO");
		return;
	}

	CmdReceiveBookingNumber *cmd = (CmdReceiveBookingNumber *)command;
	int bookingNo = cmd->getBookingNo();;
	
	if (bookingNo == 11)
	{
		Serial.println("fake server connection mode ***");
		onMsgQueClientConnected();
	}
}

void NoServerConnectState::carDetectedOnEntry(int status)
{
	if (status == CAR_DETECTED)
	{
		sendErrorToKiosk();
	}	
}

void NoServerConnectState::carDetectedOnExit(int status)
{
	if (status == CAR_DETECTED)
	{
		sendErrorToKiosk();
	}
}

void NoServerConnectState::onSlotOccupied(int slotNum)
{
	Serial.println("#$error##Network error<br>Please call dave!!$#");
}

void NoServerConnectState::onSlotEmptified(int slotNum)
{
	Serial.println("#$error##Network error<br>Please call dave!!$#");
}

void NoServerConnectState::onMsgQueClientConnected()
{
	controller->setState(controller->getWaitingState());
}

void NoServerConnectState::onMsgQueClientDisconnected()
{
}

