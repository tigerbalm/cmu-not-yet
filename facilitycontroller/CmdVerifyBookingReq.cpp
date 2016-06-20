// 
// 
// 

#include "CmdVerifyBookingReq.h"

void CmdVerifyBookingReq::setReservationNumber(int number)
{
	bookingNum = number;
}

String CmdVerifyBookingReq::getTopic()
{
	String str = "/controller/";
	str += FACILITY_ID;
	str += "/";
	str += CMD_HINT_CONFIRM_RESERVATION_REQ;
	str += "/request/";
	str += getTopicId();

	return str;
}

String CmdVerifyBookingReq::getBody()
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["confirmation_no"] = bookingNum;
	root["_msg_type_"] = 1;
	
	String body;
	root.printTo(body);

	return body;
}