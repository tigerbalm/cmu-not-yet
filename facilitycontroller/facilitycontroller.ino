#include "StateController.h"
#include "Pair.h"
#include "CommandWill.h"
#include <SimpleList.h>
#include "MsgQueClientStatusListener.h"
#include "MsgQueClient.h"
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

WiFiClient wifiClient;
PubSubClient psClient(MQ_SERVER_IP, MQ_SERVER_PORT, &psCallback, wifiClient);
MsgQueClient mqClient("Arduino-1", psClient);

Controller controller(mqClient);

Servo entryGateServo;
Servo exitGateServo;

void psCallback(char* topic, byte* payload, unsigned int length)
{
	Serial.println(topic);
	Serial.println((char *)payload);

	Command *cmd = CommandFactory::getInstance(&mqClient)->createCommand(String(topic));
	cmd->setBody((char*)payload);

	controller.receiveMessage(cmd);

	// todo : command 형태로 변경 필요
	//String message((char *)payload);
	//controller.onMessageReceived(message);
}

void startWifiConnection()
{
	int status = WL_IDLE_STATUS;

	// Attempt to connect to Wifi network.
	while (status != WL_CONNECTED)
	{
		Serial.print("Attempting to connect to SSID: ");
		Serial.println(NETWORK_SSID);
		status = WiFi.begin(NETWORK_SSID);		
	}
}

void setup()
{
	initSerial();
	
	startWifiConnection();
	mqClient.connect();

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
	controller.loop();
	mqClient.loop();

	delay(500);
}

void initSerial()
{
	Serial.begin(9600);
	if (Serial) {
		Serial.println("start facility");
	}
}