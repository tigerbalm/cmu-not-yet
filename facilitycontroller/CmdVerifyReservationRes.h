// CmdVerifyReservationRes.h

#ifndef _CMDVERIFYRESERVATIONRES_h
#define _CMDVERIFYRESERVATIONRES_h

#include "Command.h"

class CmdVerifyReservationRes : public Command
{
	int slotNumber;

	bool success;
	String failCause;
public:
	virtual void setBody(String body);

	int getSlotNumber();
	bool isSuccess();
	String getFailCause();
};
#endif

