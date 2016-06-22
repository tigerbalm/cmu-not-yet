// 
// 
// 

#include "GateHelper.h"

#define Open  90
#define Close 5

void GateHelper::initPins()
{
	gateServo.attach(pinDoor);
	off(pinLedGreen);
	off(pinLedRed);
}

void GateHelper::on(int pin)
{
	pinMode(pin, OUTPUT);
	digitalWrite(pin, LOW);
}

void GateHelper::off(int pin)
{
	pinMode(pin, OUTPUT);
	digitalWrite(pin, HIGH);
}

GateHelper::GateHelper(int _pinDoor, int _pinLedGreen, int _pinLedRed)
{
	pinDoor = _pinDoor;
	pinLedGreen = _pinLedGreen;
	pinLedRed = _pinLedRed;

	initPins();
}

void GateHelper::openDoor()
{
	ledGreen(true);
	ledRed(false);

	gateServo.write(Open);
	delay(200);	
}

void GateHelper::closeDoor()
{
	ledGreen(false);
	ledRed(true);

	gateServo.write(Close);
	delay(200);
}

void GateHelper::ledGreen(bool lightOn)
{
	if (lightOn)
	{ 
		on(pinLedGreen);
	}
	else
	{
		off(pinLedGreen);
	}	
}

void GateHelper::ledRed(bool lightOn)
{
	if (lightOn)
	{
		on(pinLedRed);
	}
	else
	{
		off(pinLedRed);
	}
}
