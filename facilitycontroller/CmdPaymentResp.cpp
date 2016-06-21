// 
// 
// 

#include "CmdPaymentResp.h"

String CmdPaymentResp::getTopic()
{
	String topic = "/controller/";
	topic += getFacilityId();
	topic += "/";
	topic += CMD_HINT_PAYMENT_RESP;
	topic += "/#";

	return topic;
}

void CmdPaymentResp::setBody(String body)
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.parseObject(body.c_str());

	int result = root["success"];

	if (result == 1)	// success
	{
		success = true;		
	}
	else
	{
		success = false;
		const char* cause = root["cause"];

		failCause += cause;
	}
}
