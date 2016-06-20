// 
// 
// 

#include "CmdAliveNoti.h"

void CmdAliveNoti::setStatus(int _status)
{
	status = _status;
}

String CmdAliveNoti::getTopic()
{
	String topic = "/controller/";
	topic += getFacilityId();

	return topic;
}

String CmdAliveNoti::getBody()
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["available"] = status;
	root["_msg_type_"] = 0;

	String body;
	root.printTo(body);

	return body;
}
