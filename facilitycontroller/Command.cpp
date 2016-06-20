// 
// 
// 

#include "Command.h"

int Command::topicId;

Command::Command()
{
}

int Command::getFacilityId()
{
	return FACILITY_ID;
}

int Command::getTopicId()
{
	return Command::topicId ++;
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

bool Command::send(MsgQueClient * _client)
{
	return _client->publish(getTopic(), getBody());
}

String Command::toString()
{
	String info;

	info += topic;
	info += "\n";
	info += body;

	return info;
}
