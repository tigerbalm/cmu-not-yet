// GateCarDetector.h

#ifndef _GATECARDETECTOR_h
#define _GATECARDETECTOR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "CarDetectedListener.h"

class GateCarDetector
{
	CarDetectedListener *carDetectedListener;
	int gatePin;
	int previousState = -1;
		
	void initPins();
public:
	GateCarDetector(CarDetectedListener *listener, int pin);
	void setup();
	void loop();
};
#endif

