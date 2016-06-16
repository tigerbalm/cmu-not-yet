// 
// 
// 

#include "CommandReportSlotStatus.h"

String COMMAND_SLOT = "slot";

CommandReportSlotStatus::CommandReportSlotStatus(NetworkManager * _manager, int _slot, int _status)
	: Command(_manager)
{
	slot = _slot;
	status = _status;
}

String CommandReportSlotStatus::getTopic()
{
	String str = "/controller/";
	str += FACILITY_ID;
	str += "/";
	str += COMMAND_SLOT;
	str += "/";
	str += slot;

	return str;
}

String CommandReportSlotStatus::getBody()
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["occupied"] = status;
	root["_msg_type_"] = 0;

	String body;
	root.printTo(body);

	return body;
}