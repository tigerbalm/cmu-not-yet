// CommandVerifyReservation.h

#ifndef _COMMANDVERIFYRESERVATION_h
#define _COMMANDVERIFYRESERVATION_h

#include "Command.h"

class CommandVerifyReservation : public Command
{
	int reservationNumber;

public:
	CommandVerifyReservation(NetworkManager *_manager) : Command(_manager) {}
	void setReservationNumber(int number);
	
	virtual String getTopic();
	virtual String getBody();
};

#endif

