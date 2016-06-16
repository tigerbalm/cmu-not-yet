// StateChangeListener.h

#ifndef _STATECHANGELISTENER_h
#define _STATECHANGELISTENER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#define STATE_WAITING	0
#define STATE_PARKING	1
#define STATE_LEAVING	2

class StateChangeListener
{
public:
	virtual void onStateChanged(int nextState) = 0;
};
#endif

