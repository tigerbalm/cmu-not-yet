// 
// 
// 

#include "CmdVerifyReservationRes.h"

void CmdVerifyReservationRes::setBody(String body)
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

int CmdVerifyReservationRes::getSlotNumber()
{
	return slotNumber;
}

bool CmdVerifyReservationRes::isSuccess()
{
	return success;
}

String CmdVerifyReservationRes::getFailCause()
{
	return failCause;
}
