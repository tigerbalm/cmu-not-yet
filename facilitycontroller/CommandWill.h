// CommandWill.h

#ifndef _COMMANDWILL_h
#define _COMMANDWILL_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "Command.h"

class CommandWill : public Command
{
public:
	CommandWill(MsgQueClient *_client) : Command(_client) {};

	virtual String getTopic();
	virtual String getBody();
};

#endif

