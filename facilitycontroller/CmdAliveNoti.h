// CommandWill.h

#ifndef _COMMANDWILL_h
#define _COMMANDWILL_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "Command.h"

#define	STATUS_DEAD		0
#define STATUS_ALIVE	1

class CmdAliveNoti : public Command
{
	int status;

public:
	void setStatus(int _status);

	String getTopic();
	String getBody();
	String getHint();

	static Command* create() { return new CmdAliveNoti(); }
};

#endif

