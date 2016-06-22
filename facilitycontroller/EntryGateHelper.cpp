// 
// 
// 

#include "EntryGateHelper.h"

#define EntryGateGreenLED 26
#define EntryGateRedLED   27

#define EntryGateServoPin 5
#define ExitGateServoPin 6
#define Open  90
#define Close 5

int delayvalue = 500;

Servo myEntryGate;

void EntryGateHelper::attach(Servo &servo) {
	myEntryGate = servo;
}

void EntryGateHelper::open()
{
	myEntryGate.write(Open);
	delay(delayvalue);
}

void EntryGateHelper::close()
{
	myEntryGate.write(Close);
	delay(delayvalue);
}

void EntryGateHelper::ledOn()
{
	ledRed(false);
	ledGreen(true);
}

void EntryGateHelper::ledOff()
{
	ledGreen(false);
	ledRed(true);
}

void EntryGateHelper::ledGreen(bool lightOn)
{	
	if (lightOn)
	{
		on(EntryGateGreenLED);
	}
	else
	{
		off(EntryGateGreenLED);
	}
}

void EntryGateHelper::ledRed(bool lightOn)
{
	if (lightOn)
	{
		on(EntryGateRedLED);
	}
	else
	{
		off(EntryGateRedLED);
	}
}

void EntryGateHelper::on(int pin)
{
	pinMode(pin, OUTPUT);
	digitalWrite(pin, LOW);
}

void EntryGateHelper::off(int pin)
{
	pinMode(pin, OUTPUT);
	digitalWrite(pin, HIGH);
}