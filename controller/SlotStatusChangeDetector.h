// SlotChangeDetector.h

#ifndef _SLOTCHANGEDETECTOR_h
#define _SLOTCHANGEDETECTOR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "SlotStatusChangeListener.h"
#include "SlotConfiguration.h"
#include "Slot.h"

class SlotStatusChangeDetector
{
	SlotStatusChangeListener *slotStatusChangeListener;
	Slot *slot[MAX_SLOT_COUNT];

	void initSlot();

public:
	SlotStatusChangeDetector(SlotStatusChangeListener *listener);

	void loop();
};

#endif

