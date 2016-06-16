// SlotChangeListener.h

#ifndef _SLOTSTATUSCHANGELISTENER_h
#define _SLOTSTATUSCHANGELISTENER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

class SlotStatusChangeListener
{
public:
	virtual void onSlotChange(int slotNumber, int status) = 0;
};
#endif

