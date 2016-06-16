// CarDetectedListener.h

#ifndef _CARDETECTEDLISTENER_h
#define _CARDETECTEDLISTENER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#define CAR_DETECTED		99
#define CAR_NOT_DETECTED	11

class CarDetectedListener
{
public:
	virtual void onCarChangeDetected(int gatePin, int status) = 0;
};

#endif

