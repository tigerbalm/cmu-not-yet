// ExitGateHelper.h

#ifndef _EXITGATEHELPER_h
#define _EXITGATEHELPER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include <Servo.h> 

class ExitGateHelper
{
public:
	static void attach(Servo & servo);
	static void open();
	static void close();
	static void ledOn();
	static void ledOff();
};
#endif

