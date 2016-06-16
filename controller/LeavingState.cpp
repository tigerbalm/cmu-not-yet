// 
// 
// 

#include "LeavingState.h"

// LEAVING �� ó�� �����ϸ� ���� EXIT GATE �� �����ϱ� ��ٸ��� ����
// �����ϸ�, ���� ��û
// ���� �Ϸ� �Ǹ� open gate
// exit gate ���� ���� �������� close gate
// wating ���� ��ȯ
#define MODE_CAR_WAITING		3000
#define	MODE_REQUEST_PAYMENT	3001
#define	MODE_OPEN_GATE			3002

void LeavingState::loop()
{
	switch (mode) {
	case MODE_REQUEST_PAYMENT:
		// send_request...
		break;
	}
}

LeavingState::LeavingState(StateChangeListener * listener, NetworkManager *manager)
{
	stateChangeListener = listener;
	networkManager = manager;
}

void LeavingState::onMessageReceived(String message)
{
	Serial.print("LeavingState::onMessageReceived: ");
	Serial.println(message);
}

void LeavingState::carDetectedOnEntry(int status)
{
	// ignore
}

void LeavingState::carDetectedOnExit(int status)
{
	// ��а� igrnore
}

void LeavingState::onSlotOccupied(int slotNum)
{
	Serial.print("LeavingState::onSlotOccupied() - ");
	Serial.println(slotNum);

	// ��� �ó�����
}

void LeavingState::onSlotEmptified(int slotNum)
{
	Serial.print("LeavingState::onSlotEmptified() - ");
	Serial.println(slotNum);

	// error ����
}