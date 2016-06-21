// CmdVerifyReservationRes.h

#ifndef _CMDVERIFYBOOKINGRESP_h
#define _CMDVERIFYBOOKINGRESP_h

#include "Response.h"

class CmdVerifyBookingResp : public Response
{
	int slotNumber;

public:
	String getTopic();
	void setBody(String body);
	int getSlotNumber();

	static Command* create() { return new CmdVerifyBookingResp(); }
};
#endif

