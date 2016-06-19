// CommandVerifyReservation.h

#ifndef _COMMANDVERIFYRESERVATION_h
#define _COMMANDVERIFYRESERVATION_h

#include "Command.h"

class CommandVerifyReservation : public Command
{
	int reservationNumber;

public:
	CommandVerifyReservation(MsgQueClient *_client) : Command(_client) {}
	void setReservationNumber(int number);
	
	virtual String getTopic();
	virtual String getBody();

	static Command* create(MsgQueClient *_client) { return new CommandVerifyReservation(_client); }
};

#endif

