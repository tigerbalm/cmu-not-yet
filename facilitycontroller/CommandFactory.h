// CommandFactory.h

#ifndef _COMMANDFACTORY_h
#define _COMMANDFACTORY_h

#include "CommandReportSlotStatus.h"
#include "CommandRequestPayment.h"
#include "CommandVerifyReservation.h"

class CommandFactory 
{
public :
	static Command get(String command);
};
#endif

