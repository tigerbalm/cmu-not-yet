#include "Slot.h"
#include "SlotStatusChangeListener.h"
#include "SlotStatusChangeDetector.h"
#include "CarDetectedListener.h"
#include <Servo.h>
#include "EntryGateHelper.h"
#include <ArduinoJson.h>
#include <WiFi.h>
#include "NetworkManagerListener.h"
#include "ClientApi.h"
#include <PubSubClient.h>
#include "NetworkManager.h"
#include "StateChangeListener.h"
#include "LeavingState.h"
#include "ParkingState.h"
#include "WatingState.h"
#include "State.h"
#include "Controller.h"

#define EntryGateServoPin 5

Controller controller;

Servo entryGateServo;
Servo exitGateServo;

void setup()
{
	initSerial();

	controller.setup();

	entryGateServo.attach(EntryGateServoPin);
	entryGateServo.write(0);
	EntryGateHelper::attach(entryGateServo);
}

void loop()
{
	Serial.println("ino.loop()");
	controller.loop();

	delay(1000);
}

void initSerial()
{
	Serial.begin(9600);
	if (Serial) {
		Serial.println("start facility");
	}
}