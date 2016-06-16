// CommandReportSlotStatus.h

#ifndef _COMMANDREPORTSLOTSTATUS_h
#define _COMMANDREPORTSLOTSTATUS_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Command.h"

class CommandReportSlotStatus : public Command
{
	int slot;
	int status;

public:
	CommandReportSlotStatus(NetworkManager *_manager, int _slot, int _status);

	virtual String getTopic();
	virtual String getBody();
};

#endif

