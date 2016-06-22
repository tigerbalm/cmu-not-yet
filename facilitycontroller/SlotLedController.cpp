// 
// 
// 

#include "SlotLedController.h"

void SlotLedController::add(int slot, int pin)
{
	SlotPinPair p;
	p.slot = slot;
	p.pin = pin;
	p.blink = false;

	slots.push_back(p);

	pinMode(pin, OUTPUT);
}

void SlotLedController::led(int pin, int onoff)
{
	if (pin == -1) 
	{
		return;
	}

	digitalWrite(pin, onoff);
}

void SlotLedController::on(int slot)
{
	int pin = findPin(slot);
	led(pin, HIGH);
}

void SlotLedController::off(int slot)
{
	int pin = findPin(slot);
	led(pin, LOW);
}

void SlotLedController::blinkOn(int slot)
{
	SlotPinPair **pair = findPair(slot);
	(*pair)->blink = true;

	blinkCount++;
}

void SlotLedController::blinkOff(int slot)
{
	SlotPinPair **pair = findPair(slot);
	(*pair)->blink = false;
	
	led((*pair)->pin, LOW);

	blinkCount--;
}

int SlotLedController::findPin(int slot)
{
	for (SimpleList<SlotPinPair>::iterator itr = slots.begin(); itr != slots.end();)
	{
		SlotPinPair pair = *itr;

		if (pair.slot == slot)
		{
			return pair.pin;
		}

		++itr;
	}

	return -1;
}

SlotPinPair** SlotLedController::findPair(int slot)
{
	for (SimpleList<SlotPinPair>::iterator itr = slots.begin(); itr != slots.end();)
	{
		SlotPinPair* pair = itr;

		if (pair->slot == slot)
		{
			return &pair;
		}

		++itr;
	}

	return NULL;
}

void SlotLedController::loop()
{	
	long now = millis();
	if (blinkCount > 0 && now - lastBlinkAttempt > 500)
	{
		Serial.println("SlotLedController::blinkloop");

		lastBlinkAttempt = now;

		for (SimpleList<SlotPinPair>::iterator itr = slots.begin(); itr != slots.end();)
		{
			SlotPinPair pair = *itr;

			if (pair.blink)
			{
				//Serial.println(pair.pin);
				//Serial.println(bBlinkOn);

				led(pair.pin, bBlinkOn ? HIGH : LOW);
			}

			++itr;
		}

		if (bBlinkOn == true) 
		{
			bBlinkOn = false; 
		}
		else
		{
			bBlinkOn = true;
		}		
	}	
}
