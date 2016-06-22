// 
// 
// 

#include "CmdPaymentReq.h"

void CmdPaymentReq::setSlot(int _slot)
{
	slot = _slot;
}

String CmdPaymentReq::getTopic()
{
	String str = "/controller/";
	str += FACILITY_ID;
	str += "/";
	str += CMD_HINT_PAYMENT_REQ;
	str += "/";
	str += getTopicId();

	return str;
}

String CmdPaymentReq::getBody()
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["slot_no"] = slot;
	root["_msg_type_"] = 1;
		
	String body;
	root.printTo(body);

	return body;
}

String CmdPaymentReq::getHint()
{
	return CMD_HINT_PAYMENT_REQ;
}
