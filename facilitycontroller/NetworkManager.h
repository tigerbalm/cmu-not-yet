// NetworkManager.h

#ifndef _NETWORKMANAGER_h
#define _NETWORKMANAGER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include <PubSubClient.h>
#include <WiFi.h>
#include <ArduinoJson.h>

#include "NetworkManagerListener.h"

#define NETWORK_SSID	"LGArchi" //"lge_archi_16"
#define MQ_SERVER_OFFICIAL	"192.168.1.21"
#define MQ_SERVER_MQTT_TEST "test.mosquitto.org"
#define MQ_SERVER_IP	MQ_SERVER_OFFICIAL
#define MQ_SERVER_PORT	1883
#define MQ_CLIENT_ID	"arduino-1"

class NetworkManager
{
	NetworkManagerListener *listener;
	WiFiClient wifiClient;
	PubSubClient *client;	// todo : replace clientapi
	
	void initNetworkClient();
	void initWifiClient();

public:
	NetworkManager(NetworkManagerListener *listener);	

	void loop();
	void send(String command, int number);
	bool send(String topic, String body);
};

#endif

