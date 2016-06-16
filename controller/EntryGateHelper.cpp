// 
// 
// 

#include "EntryGateHelper.h"

#define EntryGateGreenLED 26
#define EntryGateRedLED   27

#define EntryGateServoPin 5
#define ExitGateServoPin 6
#define Open  90
#define Close 0

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
	pinMode(EntryGateGreenLED, OUTPUT);
	//digitalWrite(EntryGateGreenLED, HIGH);

	digitalWrite(EntryGateGreenLED, LOW);
	//delay(delayvalue);
}

void EntryGateHelper::ledOff()
{
	pinMode(EntryGateRedLED, OUTPUT);
	digitalWrite(EntryGateRedLED, LOW);
}