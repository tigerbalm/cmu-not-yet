// MsgQueClientStatusListener.h

#ifndef _MSGQUECLIENTSTATUSLISTENER_h
#define _MSGQUECLIENTSTATUSLISTENER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#define MSG_QUE_CLIENT_STATUS_CONNECTED			4000
#define MSG_QUE_CLIENT_STATUS_DISCONNECTED		4001

class MsgQueClientStatusListener
{
public:
	virtual void msgQueStatusChange(int status) = 0;
};

#endif