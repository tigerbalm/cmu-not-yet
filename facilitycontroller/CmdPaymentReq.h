// CmdPaymentReq.h

#ifndef _CMDPAYMENTREQ_h
#define _CMDPAYMENTREQ_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "Request.h"

class CmdPaymentReq : public Request
{
	int slot;

public:
	void setSlot(int _slot);

	String getTopic();
	String getBody();

	static Command* create() { return new CmdPaymentReq(); }
};

#endif

