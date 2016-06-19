// 
// 
// 

#include "Command.h"

Command::Command()
{
}

Command::Command(MsgQueClient *_client)
{
	client = _client;
}

int Command::getFacilityId()
{
	return FACILITY_ID;
}

int Command::getTopicId()
{
	return topicId;
}

void Command::setTopic(String _topic)
{
	topic = _topic;
}

String Command::getTopic()
{
	return topic;
}

void Command::setBody(String _body)
{
	body = _body;
}

void Command::setBody(char *body)
{
	String b(body);

	setBody(b);
}

String Command::getBody()
{
	return String();
}

bool Command::send()
{
	return client->publish(getTopic(), getBody());
}

String Command::toString()
{
	String info;

	info += topic;
	info += "\n";
	info += body;

	return info;
}
