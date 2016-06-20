// Request.h

#ifndef _REQUEST_h
#define _REQUEST_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Command.h"
#include "MsgQueClient.h"

class Request : public Command
{
};
#endif

