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
	bool subscribe(String topic);
	bool unsubscribe(String topic);
	void unsubscribeCacheTopics(String topic);
	bool publish(String topic, String body);
	void loop();
};
#endif

