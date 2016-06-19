// 
// 
// 

#include "LeavingState.h"
#include "ExitGateHelper.h"
#include "CarDetectedListener.h"

// LEAVING 에 처음 진입하면 차가 EXIT GATE 에 도착하길 기다리는 상태
// 도착하면, 결제 요청
// 결제 완료 되면 open gate
// exit gate 에서 차가 없어지면 close gate
// wating 으로 전환
#define MODE_CAR_WAITING		3000
#define	MODE_REQUEST_PAYMENT	3001
#define	MODE_OPEN_GATE			3002

void LeavingState::loop()
{
	Serial.println("LeavingState");

	switch (mode) {
	case MODE_REQUEST_PAYMENT:
		// send_request...
		break;
	}
}

void LeavingState::onMessageReceived(Command *command)
{
	Serial.println("LeavingState::onMessageReceived: ");
}

void LeavingState::carDetectedOnEntry(int status)
{
	// ignore
}

void LeavingState::carDetectedOnExit(int status)
{
	if (status == CAR_DETECTED)
	{
		ExitGateHelper::ledOn();
		ExitGateHelper::open();	
	}
	else
	{
		ExitGateHelper::ledOff();
		ExitGateHelper::close();

		stateChangeListener->onStateChanged(STATE_WAITING);
	}
}

void LeavingState::onSlotOccupied(int slotNum)
{
	Serial.print("LeavingState::onSlotOccupied() - ");
	Serial.println(slotNum);

	// 취소 시나리오
}

void LeavingState::onSlotEmptified(int slotNum)
{
	Serial.print("LeavingState::onSlotEmptified() - ");
	Serial.println(slotNum);

	// error 상태
}