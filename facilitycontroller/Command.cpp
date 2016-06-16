// 
// 
// 

#include "Command.h"

Command::Command()
{
}

Command::Command(NetworkManager * manager)
{
	networkManager = manager;
}

int Command::getFacilityId()
{
	return FACILITY_ID;
}

int Command::getTopicId()
{
	return topicId;
}

void Command::setTopic(String topic)
{
}

String Command::getTopic()
{
	return String();
}

void Command::setBody(String body)
{
}

String Command::getBody()
{
	return String();
}

bool Command::send()
{
	return networkManager->send(getTopic(), getBody());
}
