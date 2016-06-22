#include "GateHelper.h"
#include "CmdExceptionNoti.h"
#include "CmdReceiveBookingNumber.h"
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
// refer to http://pubsubclient.knolleary.net/
PubSubClient psClient(MQ_SERVER_IP, MQ_SERVER_PORT, &psCallback, wifiClient);
MsgQueClient mqClient(MQ_CLIENT_ID, psClient);

Controller controller(mqClient);

Servo entryGateServo;
Servo exitGateServo;

void psCallback(char* topic, byte* payload, unsigned int length)
{	
	Serial.println(topic);
	Serial.println((char *)payload);

	Command *cmd = CommandFactory::getInstance()->createCommand(String(topic));
	cmd->setBody((char*)payload);

	controller.receiveMessage(cmd);

	delete cmd;
}

void setup()
{
	setupSerial();
	
	setupNetwork();
	
	setupMqClient();
	
	setupController();
	
	setupDevice();
}

void stringCallback(char *myString)
{
	//Firmata.sendString(myString);
	Serial.println(myString);
}

void setupSerial()
{
	Serial.begin(57600);
	
	while (!Serial) { ; }

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

	GateHelper::entryGate()->closeDoor();
	GateHelper::exitGate()->closeDoor();

	//entryGateServo.attach(EntryGateServoPin);
	//EntryGateHelper::attach(entryGateServo);
	//EntryGateHelper::close();
	//EntryGateHelper::ledOff();

	//exitGateServo.attach(ExitGateServoPin);
	//ExitGateHelper::attach(exitGateServo);
	//ExitGateHelper::close();
	//ExitGateHelper::ledOff();

	Serial.println("setupDevice - end");
}

void loop()
{
	SlotLedController::getInstance()->loop();
	controller.loop();
	mqClient.loop();

	delay(100);
}

String inputString = "";
String *copiedInputString;
boolean stringComplete = false;

/*
SerialEvent occurs whenever a new data comes in the
hardware serial RX.  This routine is run between each
time loop() runs, so using delay inside loop can delay
response.  Multiple bytes of data may be available.
*/
void serialEvent() {
	Serial.println("serialEvent is called.");

	while (Serial.available()) {
		// get the new byte:
		char inChar = (char)Serial.read();
		
		// if the incoming character is a newline, set a flag
		// so the main loop can do something about it:
		if (inChar == '\n') {
			//stringComplete = true;
			//copiedInputString = new String(inputString);
			handleSerialInputByLine(inputString);

			inputString = "";
		} else {
			// add it to the inputString:
			inputString += inChar;
		}
	}
}

// #topic##body#
void handleSerialInputByLine(String &line) 
{
	Serial.println(line);

	if (line.startsWith("#") == 0 || line.endsWith("#") == 0)
	{
		Serial.println("not vaild format - #topic##body#");
		return;
	}

	// extract topic
	String topic = line.substring(1, line.indexOf("##"));
	String body = line.substring(line.indexOf("##") + 2, line.length() - 1);

	psCallback((char *)topic.c_str(), (byte *)body.c_str(), body.length());
}
