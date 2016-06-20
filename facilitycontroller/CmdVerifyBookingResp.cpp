// 
// 
// 

#include "CmdVerifyBookingResp.h"

void CmdVerifyBookingResp::setBody(String body)
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.parseObject(body.c_str());

	int result = root["success"];

	if (result == 1)	// success
	{
		success = true;
		slotNumber = root["slot_no"];
	}
	else
	{
		success = false;
		const char* cause = root["cause"];

		failCause += cause;
	}
}

int CmdVerifyBookingResp::getSlotNumber()
{
	return slotNumber;
}
