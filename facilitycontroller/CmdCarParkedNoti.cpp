// 
// 
// 

#include "CmdCarParkedNoti.h"

void CmdCarParkedNoti::setData(int _bookingNo, int _slot)
{
	bookingNo = _bookingNo;
	slot = _slot;
}

String CmdCarParkedNoti::getTopic()
{
	String str = "/controller/";
	str += FACILITY_ID;
	str += "/";
	str += CMD_HINT_CAR_PARKED_NOTIFY;
	str += "/";
	str += slot;

	return str;
}

String CmdCarParkedNoti::getBody()
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["parked"] = 1;
	root["reservation_no"] = bookingNo;
	root["_msg_type_"] = 0;

	String body;
	root.printTo(body);

	return body;
}
