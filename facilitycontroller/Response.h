// Response.h

#ifndef _RESPONSE_h
#define _RESPONSE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Command.h"

class Response : public Command
{
protected:
	bool success;
	String failCause;

public:
	virtual String getTopic() = 0;
	virtual void setBody(String body) = 0;
	virtual bool isSuccess() { return success; };
	virtual String getFailCause() { return failCause; };
};

#endif

