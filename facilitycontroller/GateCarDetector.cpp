// 
// 
// 

#include "GateCarDetector.h"
#include "SureParkPinHeader.h"

#define PIN_STATE_DETECTED	LOW

void GateCarDetector::initPins()
{
	Serial.print("GateCarDetector::initPins");
	Serial.println(gatePin);

	pinMode(gatePin, INPUT);     // Make entry IR rcvr an input
	digitalWrite(gatePin, HIGH); // enable the built-in pullup

}

GateCarDetector::GateCarDetector(CarDetectedListener *listener, int pin)
{
	this->carDetectedListener = listener;
	this->gatePin = pin;
	
	initPins();
}

void GateCarDetector::setup()
{
}

void GateCarDetector::loop()
{
	if (carDetectedListener == NULL)
	{
		Serial.println("carDetectedListener is null.");
		return;
	}

	//Serial.print("start beam sensing... ");
	//Serial.println(gatePin);

	int state = digitalRead(gatePin);

	if (state == previousState) {
		return;
	}

	if (state == PIN_STATE_DETECTED) {
		Serial.println("car is detected on gate.");
		carDetectedListener->onCarChangeDetected(gatePin, CAR_DETECTED);
	}
	else 
	{
		Serial.println("no car detected...");
		carDetectedListener->onCarChangeDetected(gatePin, CAR_NOT_DETECTED);
	}

	previousState = state;
}
