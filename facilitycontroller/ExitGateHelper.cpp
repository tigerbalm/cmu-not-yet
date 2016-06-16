// 
// 
// 

#include "ExitGateHelper.h"

#define GateGreenLED 28
#define GateRedLED   29

#define Open  90
#define Close 5

//int delayvalue = 500;

Servo myExitGate;

void ExitGateHelper::attach(Servo &servo) {
	myExitGate = servo;
}

void ExitGateHelper::open()
{
	myExitGate.write(Open);
	delay(500);
}

void ExitGateHelper::close()
{
	myExitGate.write(Close);
	delay(500);
}

void ExitGateHelper::ledOn()
{
	pinMode(GateRedLED, OUTPUT);
	digitalWrite(GateRedLED, HIGH);

	pinMode(GateGreenLED, OUTPUT);	
	digitalWrite(GateGreenLED, LOW);	
}

void ExitGateHelper::ledOff()
{
	pinMode(GateGreenLED, OUTPUT);
	digitalWrite(GateGreenLED, HIGH);

	pinMode(GateRedLED, OUTPUT);
	digitalWrite(GateRedLED, LOW);
}