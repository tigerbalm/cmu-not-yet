// 
// 
// 

#include "CmdReceiveBookingNumber.h"

void CmdReceiveBookingNumber::setBody(String body)
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.parseObject(body.c_str());

	bookingNo = root["confirmation_no"];	
}

int CmdReceiveBookingNumber::getBookingNo()
{
	return bookingNo;
}

String CmdReceiveBookingNumber::getHint()
{
	return CMD_HINT_RECEIVE_BOOK_NO;
}
