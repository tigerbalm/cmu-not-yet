// 
// 
// 

#include "CmdExceptionNoti.h"

void CmdExceptionNoti::setMessage(String _message)
{
	message = _message;
}

String CmdExceptionNoti::getTopic()
{
	String topic = "/controller/";
	topic += getFacilityId();
	topic += "/error";
	
	return topic;
}

String CmdExceptionNoti::getBody()
{
	StaticJsonBuffer<300> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	
	root["message"] = message;
	root["_msg_type_"] = 0;

	String body;
	root.printTo(body);

	return body;
}

String CmdExceptionNoti::getHint()
{
	return CMD_HINT_EXCEPTION_NOTIFY;
}
