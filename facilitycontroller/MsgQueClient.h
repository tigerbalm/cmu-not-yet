// MsgQueClient.h

#ifndef _MSGQUECLIENT_h
#define _MSGQUECLIENT_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif
#include <SimpleList.h>
#include <PubSubClient.h>

#include "MsgQueClientStatusListener.h"

#define NETWORK_SSID	"LGArchi" //"lge_archi_16"
#define MQ_SERVER_OFFICIAL	"192.168.1.21"
#define MQ_SERVER_MQTT_TEST "test.mosquitto.org"
#define MQ_SERVER_IP	MQ_SERVER_OFFICIAL
#define MQ_SERVER_PORT	1883
#define MQ_CLIENT_ID	"arduino-1"

class MsgQueClient
{
	SimpleList<String> cacheTopics;

	String clientName;
	long lastReconnectAttempt;	
	
	MsgQueClientStatusListener *listener;	
	PubSubClient *pubsubClient;

	bool reconnect();
	void subscribeCacheTopics();
	bool connected();
public:	
	MsgQueClient(char *clinetName, PubSubClient &pubsubClient);
	
	bool connect();
	bool disconnect();
	bool subscribeAll();
	bool subscribe(String topic);
	bool unsubscribe(String topic);
	void unsubscribeCacheTopics(String topic);
	bool publish(String topic, String body);
	void loop();
};
#endif

