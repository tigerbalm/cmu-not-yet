// CommandFactory.h

#ifndef _COMMANDFACTORY_h
#define _COMMANDFACTORY_h

#include "CommandReportSlotStatus.h"
#include "CommandRequestPayment.h"
#include "CommandVerifyReservation.h"

#include "Pair.h"

#define CMD_HINT_RESERVATION				"confirm_reservation"
#define CMD_HINT_RESERVATION_RESPONSE		"confirm_reservation"

class CommandFactory 
{
	SimpleList<Pair> factoryMap;
	MsgQueClient *mqClient;

	void init();
	void add(char *hint, CreateCommandFn pfnCreate);
	CreateCommandFn* find(String topic);

	CommandFactory(MsgQueClient *_mqClient);
public :
	~CommandFactory() { factoryMap.clear(); }

	static CommandFactory *getInstance(MsgQueClient *_mqClient)
	{
		static CommandFactory instance(_mqClient);
		return &instance;
	}
	
	Command *createCommand(const String topic);
};
#endif

