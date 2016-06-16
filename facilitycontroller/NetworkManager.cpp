// 
// 
// 
#include "NetworkManager.h"
#include "Controller.h"

NetworkManager::NetworkManager(NetworkManagerListener * listener)
{
	this->listener = listener;

	initNetworkClient();
	initWifiClient();
}

void NetworkManager::initNetworkClient()
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

void callback(char* topic, byte* payload, unsigned int length)
{
	extern Controller controller;

	Serial.println(topic);
	Serial.println((char *)payload);
	
	// todo : command 형태로 변경 필요
	String message((char *)payload);
	controller.onMessageReceived(message);
}

void NetworkManager::initWifiClient()
{
	IPAddress addr;
	addr.fromString(MQ_SERVER_IP);
	this->client = new PubSubClient(addr, MQ_SERVER_PORT, &callback, wifiClient);

	Serial.print("Attempting to connect to MessageServer : ");
	Serial.print(MQ_SERVER_IP ", ");
	Serial.println(MQ_SERVER_PORT);

	if (client->connect(MQ_CLIENT_ID))
	{
		Serial.println("Connected to MessageServer");

		if (client->subscribe("/controller/1/confirm_reservation/response/#"))
		{
			Serial.println("Subscribe ok");
		} 
		else
		{
			Serial.println("Subscribe failed");
		}
	}
	else
	{
		Serial.println("MQTT connect failed - just test mode");
		//abort();
	}
}

void NetworkManager::loop()
{
	client->loop();
}

void NetworkManager::send(String command, int number)
{
	// 우선은 verify 예약만, command 객체를 만들어서 각자 보내도록 수정 필요
	StaticJsonBuffer<200> jsonBuffer;

	JsonObject& root = jsonBuffer.createObject();
	root["confirmation_no"] = number;	
	root["_msg_type_"] = 1;

	String body;
	root.printTo(body);

	Serial.print("/controller/1/confirm_reservation/request/1 : ");
	Serial.println(body);

	bool success = client->publish("/controller/1/confirm_reservation/request/1", body.c_str());
	
	if (success) 
	{
		Serial.println("publish success");
	}
}

bool NetworkManager::send(String topic, String body)
{
	Serial.println("NetworkManager::send()");
	Serial.println(topic);
	Serial.println(body);

	return client->publish(topic.c_str(), body.c_str());
}
