// 
// 
// 

#include "Slot.h"

Slot::Slot(int pin)
{
	this->pin = pin;
}

bool Slot::changed()
{
	return prevStatus != currStatus;
}

void Slot::refresh()
{	
	prevStatus = currStatus;

	if (proximityVal(pin) < PROXIMITY_THRESHOLD)
	{
		currStatus = SLOT_OCCUPIED;
	}
	else
	{
		currStatus = SLOT_EMPTIFIED;
	}
}

int Slot::status()
{
	return currStatus;
}

/*********************************************************************
* long ProximityVal(int Pin)
* Parameters:
* int pin - the pin on the Arduino where the QTI sensor is connected.
*
* Description:
* QTI schematics and specs: http://www.parallax.com/product/555-27401
* This method initalizes the QTI sensor pin as output and charges the
* capacitor on the QTI. The QTI emits IR light which is reflected off
* of any surface in front of the sensor. The amount of IR light
* reflected back is detected by the IR resistor on the QTI. This is
* the resistor that the capacitor discharges through. The amount of
* time it takes to discharge determines how much light, and therefore
* the lightness or darkness of the material in front of the QTI sensor.
* Given the closeness of the object in this application you will get
* 0 if the sensor is covered
***********************************************************************/
long Slot::proximityVal(int pin)
{
	long duration = 0;
	pinMode(pin, OUTPUT);         // Sets pin as OUTPUT
	digitalWrite(pin, HIGH);      // Pin HIGH
	delay(1);                     // Wait for the capacitor to stabilize

	pinMode(pin, INPUT);          // Sets pin as INPUT
	digitalWrite(pin, LOW);       // Pin LOW
	while (digitalRead(pin))       // Count until the pin goes
	{                             // LOW (cap discharges)
		duration++;
	}
	return duration;              // Returns the duration of the pulse
}