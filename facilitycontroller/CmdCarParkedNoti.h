// CmdCarParkedNoti.h

#ifndef _CMDCARPARKEDNOTI_h
#define _CMDCARPARKEDNOTI_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "Command.h"

class CmdCarParkedNoti : public Command
{
	int slot;
	int bookingNo;

public:
	void setData(int _bookingNo, int _slot);

	String getTopic();
	String getBody();
	String getHint();

	static Command* create() { return new CmdCarParkedNoti(); }
};

#endif

