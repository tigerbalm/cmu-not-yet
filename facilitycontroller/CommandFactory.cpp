// 
// 
// 

#include "CommandFactory.h"
#include "CmdVerifyReservationRes.h"

// http://www.codeproject.com/Articles/363338/Factory-Pattern-in-Cplusplus

void CommandFactory::init()
{
	add(CMD_HINT_RESERVATION, &CommandVerifyReservation::create);
}

CreateCommandFn * CommandFactory::find(String topic)
{
	for (SimpleList<Pair>::iterator itr = factoryMap.begin(); itr != factoryMap.end();)
	{
		Pair pair = *itr;

		if (topic.indexOf(pair.hint) > 0)
		{
			return &pair.pfnCreate;
		}
	}

	return NULL;
}

void CommandFactory::add(char *hint, CreateCommandFn pfnCreate)
{
	Pair p(hint, pfnCreate);

	factoryMap.push_back(p);
}

CommandFactory::CommandFactory(MsgQueClient * _mqClient)
{
	mqClient = _mqClient;

	init();
}

Command * CommandFactory::createCommand(const String topic)
{
	CreateCommandFn *pCreateFnc = find(topic);

	if (pCreateFnc == NULL) {
		return new Command();
	}

	return (*pCreateFnc)(mqClient);
}