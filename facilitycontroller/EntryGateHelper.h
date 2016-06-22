// EntryGateHelper.h

#ifndef _ENTRYGATEHELPER_h
#define _ENTRYGATEHELPER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include <Servo.h> 

class EntryGateHelper
{
public:
	static void attach(Servo & servo);
	static void open();
	static void close();
	static void ledOn();
	static void ledOff();
	static void ledGreen(bool on);
	static void ledRed(bool on);
	static void on(int pin);
	static void off(int pin);
};
#endif

