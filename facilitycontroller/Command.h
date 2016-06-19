// Command.h

#ifndef _COMMAND_h
#define _COMMAND_h
#include <WString.h>
#include <ArduinoJson.h>
#include "MsgQueClient.h"
#include "FacilityConfiguration.h"

class Command
{	
protected:
	MsgQueClient *client;

	int topicId = 0;
	String topic;
	String body;
public:
	Command();
	Command(MsgQueClient *_client);

	int getFacilityId();
	int getTopicId();

	void setTopic(String topic);
	virtual String getTopic();

	void setBody(String body);
	void setBody(char * body);
	virtual String getBody();

	bool send();
	
	String toString();
};
#endif

