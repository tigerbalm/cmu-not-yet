// State.h

#ifndef _STATE_h
#define _STATE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

class State
{
public:
	virtual void loop() =0;
	virtual void onMessageReceived(String message) = 0;
	virtual void carDetectedOnEntry(int status) = 0;
	virtual void carDetectedOnExit(int status) = 0;
	virtual void onSlotOccupied(int slotNum) = 0;
	virtual void onSlotEmptified(int slotNum) = 0;
};
#endif

