// CmdExceptionNoti.h

#ifndef _CMDEXCEPTIONNOTI_h
#define _CMDEXCEPTIONNOTI_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "Command.h"

class CmdExceptionNoti : public Command
{
	String message;

public:
	void setMessage(String &_message);

	String getTopic();
	String getBody();
	String getHint();

	static Command* create() { return new CmdExceptionNoti(); }
};
#endif

