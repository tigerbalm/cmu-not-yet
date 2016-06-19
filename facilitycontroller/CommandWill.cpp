// 
// 
// 

#include "CommandWill.h"

String CommandWill::getTopic()
{
	String willTopic = "/controller/";
	willTopic += getFacilityId();

	return willTopic;
}

String CommandWill::getBody()
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["available"] = 0;
	root["_msg_type_"] = 0;

	String body;
	root.printTo(body);

	return body;
}
