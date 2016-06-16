// 
// 
// 

#include "CommandVerifyReservation.h"

String COMMAND_VERIFY_RESERVATION = "confirm_reservation";

void CommandVerifyReservation::setReservationNumber(int number)
{
	reservationNumber = number;
}

String CommandVerifyReservation::getTopic()
{
	String str = "/controller/";
	str += FACILITY_ID;
	str += "/";
	str += COMMAND_VERIFY_RESERVATION;
	str += "/request/";
	str += topicId ++;

	return str;
}

String CommandVerifyReservation::getBody()
{
	StaticJsonBuffer<100> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["confirmation_no"] = reservationNumber;
	root["_msg_type_"] = 1;
	
	String body;
	root.printTo(body);

	return body;
}