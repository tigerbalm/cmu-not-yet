// CmdPaymentResp.h

#ifndef _CMDPAYMENTRESP_h
#define _CMDPAYMENTRESP_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Response.h"

class CmdPaymentResp : public Response
{	
public:
	String getTopic();
	void setBody(String body);
	String getHint();
	
	static Command* create() { return new CmdPaymentResp(); }
};

#endif

