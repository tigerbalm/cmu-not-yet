// Slot.h

#ifndef _SLOT_h
#define _SLOT_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#define SLOT_UNDEFINED	-1
#define SLOT_OCCUPIED	1000
#define SLOT_EMPTIFIED	1001

#define PROXIMITY_THRESHOLD		30

class Slot
{
	int pin;

	int currStatus = SLOT_UNDEFINED;
	int prevStatus = SLOT_UNDEFINED;

	long proximityVal(int pin);
public:
	Slot(int pin);

	bool changed();
	void refresh();
	int status();
};

#endif

