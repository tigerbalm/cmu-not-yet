// 
// 
// 

#include "MsgQueClient.h"
#include "CmdAliveNoti.h"
#include "CommandFactory.h"

MsgQueClient::MsgQueClient(char* name, PubSubClient &client)
{
	clientName = String(name);
	pubsubClient = &client;
}

bool MsgQueClient::connect()
{
	Serial.print("MsgQueClient::connect to ");	
	Serial.println(MQ_SERVER_IP);

	CmdAliveNoti* will = (CmdAliveNoti *)CommandFactory::getInstance()->createCommand(CMD_HINT_MY_STATUS_NOTIFY);
	will->setStatus(STATUS_DEAD);

	bool connected = pubsubClient->connect(clientName.c_str(), 
						will->getTopic().c_str(), 1, true, will->getBody().c_str());

	if (connected)
	{
		CmdAliveNoti* alive = (CmdAliveNoti *)CommandFactory::getInstance()->createCommand(CMD_HINT_MY_STATUS_NOTIFY);
		alive->setStatus(STATUS_ALIVE);
		alive->send(this);

		Serial.println("connection success!!");
	}
	else
	{
		Serial.print("connection failed : ");
		Serial.println(pubsubClient->state());
	}

	return connected;
}

bool MsgQueClient::disconnect()
{
	pubsubClient->disconnect();

	return connected();
}

bool MsgQueClient::subscribe(String topic)
{
	if (!connected())
	{
		cacheTopics.push_back(topic);
		return true;
	}

	return pubsubClient->subscribe(topic.c_str());	
}

bool MsgQueClient::unsubscribe(String topic)
{
	if (!connected())
	{
		unsubscribeCacheTopics(topic);
		return true;
	}

	return pubsubClient->unsubscribe(topic.c_str());
}

void MsgQueClient::unsubscribeCacheTopics(String topic)
{
	if (cacheTopics.empty())
	{
		return;
	}

	for (SimpleList<String>::iterator itr = cacheTopics.begin(); itr != cacheTopics.end();)
	{
		String t = *itr;

		if (topic.equals(t))
		{
			cacheTopics.erase(itr);
			return;
		}		
	}
}

bool MsgQueClient::publish(String topic, String body)
{
	Serial.println("MsgQueClient::publish()");
	
	Serial.println(topic);
	Serial.println(body);

	pubsubClient->publish(topic.c_str(), body.c_str());
}

void MsgQueClient::subscribeCacheTopics()
{
	if (cacheTopics.empty())
	{
		return;
	}
		
	for (SimpleList<String>::iterator itr = cacheTopics.begin(); itr != cacheTopics.end();)
	{
		String t = *itr;

		pubsubClient->subscribe(t.c_str());

		cacheTopics.erase(itr);
	}
}

bool MsgQueClient::connected()
{
	return pubsubClient->connected();
}

void MsgQueClient::loop()
{
	if (!connected())
	{
		long now = millis();
		if (now - lastReconnectAttempt > 600000)
		{
			lastReconnectAttempt = now;
			// Attempt to reconnect
			if (reconnect())
			{
				lastReconnectAttempt = 0;
			}
		}
	}
	else 
	{	
		pubsubClient->loop();
	}
}

boolean MsgQueClient::reconnect() {
	Serial.println("MsgQueClient::reconnect()");

	if (connect())
	{
		subscribeCacheTopics();
	}

	return connected();
}