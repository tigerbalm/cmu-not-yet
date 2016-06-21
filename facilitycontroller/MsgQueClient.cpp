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
	if (will == NULL)
	{
		Serial.println("will is null");
		return false;
	}

	will->setStatus(STATUS_DEAD);

	Serial.println(will->getTopic());
	Serial.println(will->getBody());

	bool connected = pubsubClient->connect(clientName.c_str(), 
						will->getTopic().c_str(), 1, false, will->getBody().c_str());

	if (connected)
	{
		subscribeAll();

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

bool MsgQueClient::subscribeAll()
{
	Command * reservation = CommandFactory::getInstance()->createCommand(CMD_HINT_CONFIRM_RESERVATION_RESP);
	pubsubClient->subscribe(reservation->getTopic().c_str());
	Serial.print("subscribe : ");
	Serial.println(reservation->getTopic().c_str());
	delete reservation;

	Command * payment = CommandFactory::getInstance()->createCommand(CMD_HINT_PAYMENT_RESP);
	Serial.print("subscribe : ");
	Serial.println(payment->getTopic().c_str());
	delay(2000);
	pubsubClient->subscribe(payment->getTopic().c_str());
	delete payment;
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

		++itr;
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

		++itr;
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