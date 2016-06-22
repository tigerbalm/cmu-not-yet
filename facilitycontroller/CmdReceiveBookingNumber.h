// CmdReceiveBookingNumber.h

#ifndef _CMDRECEIVEBOOKINGNUMBER_h
#define _CMDRECEIVEBOOKINGNUMBER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "Command.h"

class CmdReceiveBookingNumber : public Command
{
	int bookingNo;

public:
	void setBody(String body);
	int getBookingNo();
	String getHint();

	static Command* create() { return new CmdReceiveBookingNumber(); }
};

#endif

