// Command.h

#ifndef _COMMAND_h
#define _COMMAND_h
#include <WString.h>
#include <ArduinoJson.h>
#include "NetworkManager.h"
#include "FacilityConfiguration.h"

class Command
{
	NetworkManager *networkManager;

protected:
	int topicId = 0;
	String topic;
	String body;
public:
	Command();
	Command(NetworkManager *manager);

	int getFacilityId();
	int getTopicId();

	void setTopic(String topic);
	virtual String getTopic();

	void setBody(String body);
	virtual String getBody();

	bool send();
};
#endif

