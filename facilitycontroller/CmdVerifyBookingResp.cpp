// 
// 
// 

#include "CmdVerifyBookingResp.h"

String CmdVerifyBookingResp::getTopic()
{
	Serial.println("CmdVerifyBookingResp::getTopic()");

	String topic = "/controller/";
	topic += getFacilityId();
	topic += "/";
	topic += CMD_HINT_CONFIRM_RESERVATION_RESP;
	topic += "/#";

	return topic;
}

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
