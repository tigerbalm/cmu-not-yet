// LedController.h

#ifndef _SLOTLEDCONTROLLER_h
#define _SLOTLEDCONTROLLER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include <SimpleList.h>

struct SlotPinPair
{
	int slot;
	int pin;
	bool blink;
};

class SlotLedController
{
	SimpleList<SlotPinPair> slots;
	long lastBlinkAttempt = 0;
	int blinkCount = 0;
	bool bBlinkOn = true;

	void led(int pin, int onoff);

	int findPin(int slot);
	SlotPinPair * findPair(int slot);
public:
	static SlotLedController *getInstance()
	{
		static SlotLedController instance;
		return &instance;
	}

	void add(int slot, int pin);
	
	void on(int slot);
	void off(int slot);

	void blinkOn(int slot);
	void blinkOff(int slot);

	void loop();
};

#endif

