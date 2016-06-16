#include "CmdVerifyReservationRes.h"
#include "FacilityConfiguration.h"
#include "CommandRequestPayment.h"
#include "CommandReportSlotStatus.h"
#include "CommandVerifyReservation.h"
#include "CommandFactory.h"
#include "Command.h"
#include "Slot.h"
#include "SlotStatusChangeListener.h"
#include "SlotStatusChangeDetector.h"
#include "CarDetectedListener.h"
#include <Servo.h>
#include "ExitGateHelper.h"
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
#define ExitGateServoPin 6

Controller controller;

Servo entryGateServo;
Servo exitGateServo;

void setup()
{
	initSerial();

	controller.setup();

	entryGateServo.attach(EntryGateServoPin);	
	EntryGateHelper::attach(entryGateServo);
	EntryGateHelper::close();
	EntryGateHelper::ledOff();

	exitGateServo.attach(ExitGateServoPin);	
	ExitGateHelper::attach(exitGateServo);
	ExitGateHelper::close();
	ExitGateHelper::ledOff();
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