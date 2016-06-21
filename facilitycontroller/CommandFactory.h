// CommandFactory.h

#ifndef _COMMANDFACTORY_h
#define _COMMANDFACTORY_h

#include "Command.h"
#include "Pair.h"

class CommandFactory 
{
	SimpleList<Pair> factoryMap;

	void init();
	void add(char *hint, CreateCommandFn pfnCreate);
	CreateCommandFn find(String topic);

	CommandFactory();
public :
	~CommandFactory() { factoryMap.clear(); }

	static CommandFactory *getInstance()
	{
		static CommandFactory instance;
		return &instance;
	}
	
	Command *createCommand(const String topic);
};
#endif

