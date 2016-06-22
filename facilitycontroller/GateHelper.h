// GateHelper.h

#ifndef _GATEHELPER_h
#define _GATEHELPER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include "Servo.h"

#define EntryGateGreenLED	26
#define EntryGateRedLED		27
#define ExitGateGreenLED	28
#define ExitGateRedLED		29

#define EntryGateServoPin	5
#define ExitGateServoPin	6

class GateHelper {
	int pinDoor;
	int pinLedGreen;
	int pinLedRed;

	Servo gateServo;

	void initPins();
	void on(int pin);
	void off(int pin);
public:
	GateHelper(int _pinDoor, int _pinLedGreen, int _pinLedRed);
	void openDoor();
	void closeDoor();

	void ledGreen(bool on);
	void ledRed(bool on);

	static GateHelper* entryGate() 
	{ 
		static GateHelper entryGateInstance(EntryGateServoPin, EntryGateGreenLED, EntryGateRedLED);
		return &entryGateInstance;
	}

	static GateHelper* exitGate()
	{
		static GateHelper exitGateInstance(ExitGateServoPin, ExitGateGreenLED, ExitGateRedLED);
		return &exitGateInstance;
	}
};
#endif

