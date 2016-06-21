#include "SlotLedController.h"
#include "CmdCarParkedNoti.h"
#include "Request.h"
#include "Response.h"
#include "CmdPaymentResp.h"
#include "CmdPaymentReq.h"
#include "Pair.h"
#include "CmdAliveNoti.h"
#include <SimpleList.h>
#include "MsgQueClientStatusListener.h"
#include "MsgQueClient.h"
#include "CmdVerifyBookingReq.h"
#include "FacilityConfiguration.h"
#include "CommandRequestPayment.h"
#include "CommandReportSlotStatus.h"
#include "CmdVerifyBookingResp.h"
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
#include "ClientApi.h"
#include <PubSubClient.h>
#include "StateChangeListener.h"
#include "LeavingState.h"
#include "ParkingState.h"
#include "WatingState.h"
#include "State.h"
#include "Controller.h"
#include "SureParkPinHeader.h"

#define EntryGateServoPin 5
#define ExitGateServoPin 6

WiFiClient wifiClient;
PubSubClient psClient(MQ_SERVER_IP, MQ_SERVER_PORT, &psCallback, wifiClient);
MsgQueClient mqClient(MQ_CLIENT_ID, psClient);

Controller controller(mqClient);

Servo entryGateServo;
Servo exitGateServo;

void psCallback(char* topic, byte* payload, unsigned int length)
{	
//	return;

	Serial.println(topic);
	Serial.println((char *)payload);

	Command *cmd = CommandFactory::getInstance()->createCommand(String(topic));
	cmd->setBody((char*)payload);

	controller.receiveMessage(cmd);
}

void setup()
{
	setupSerial();
	
	setupNetwork();
	
	setupMqClient();
	
	setupController();
	
	setupDevice();
}

void setupSerial()
{
	Serial.begin(9600);
	if (Serial) {
		Serial.println("start facility");
	}
}

void setupNetwork()
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

void setupMqClient()
{
	if (mqClient.connect())
	{
		CmdAliveNoti* alive = (CmdAliveNoti *)CommandFactory::getInstance()->createCommand(CMD_HINT_MY_STATUS_NOTIFY);
		alive->setStatus(STATUS_ALIVE);
		alive->send(&mqClient);
	}	
}

void setupController()
{
	controller.setup();
}

void setupDevice()
{
	Serial.println("setupDevice - start");

	// LED
	SlotLedController::getInstance()->add(1, PARKING_STALL1_LED);
	SlotLedController::getInstance()->add(2, PARKING_STALL2_LED);
	SlotLedController::getInstance()->add(3, PARKING_STALL3_LED);
	SlotLedController::getInstance()->add(4, PARKING_STALL4_LED);

	entryGateServo.attach(EntryGateServoPin);
	EntryGateHelper::attach(entryGateServo);
	EntryGateHelper::close();
	EntryGateHelper::ledOff();

	exitGateServo.attach(ExitGateServoPin);
	ExitGateHelper::attach(exitGateServo);
	ExitGateHelper::close();
	ExitGateHelper::ledOff();

	Serial.println("setupDevice - end");
}

void loop()
{
	//Serial.println("loop()");

	SlotLedController::getInstance()->loop();
	controller.loop();
	mqClient.loop();

	delay(500);
}
