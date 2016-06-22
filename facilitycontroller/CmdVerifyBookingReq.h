// CommandVerifyReservation.h

#ifndef _CMDVERIFYBOOKINGREQ_h
#define _CMDVERIFYBOOKINGREQ_h

#include "Request.h"

class CmdVerifyBookingReq : public Request
{
	int bookingNum;

public:
	void setReservationNumber(int number);
	
	String getTopic();
	String getBody();
	String getHint();

	static Command* create() { return new CmdVerifyBookingReq(); }
};

#endif

