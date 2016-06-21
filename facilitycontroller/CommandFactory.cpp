// 
// 
// 

#include "CommandFactory.h"

#include "CmdVerifyBookingReq.h"
#include "CmdVerifyBookingResp.h"
#include "CmdPaymentReq.h"
#include "CmdPaymentResp.h"
#include "CmdCarParkedNoti.h"
#include "CmdAliveNoti.h"

void CommandFactory::init()
{
	add(CMD_HINT_CONFIRM_RESERVATION_REQ, &CmdVerifyBookingReq::create);
	add(CMD_HINT_CONFIRM_RESERVATION_RESP, &CmdVerifyBookingResp::create);
	
	add(CMD_HINT_PAYMENT_REQ, &CmdPaymentReq::create);
	add(CMD_HINT_PAYMENT_RESP, &CmdPaymentResp::create);

	add(CMD_HINT_CAR_PARKED_NOTIFY, &CmdCarParkedNoti::create);
	add(CMD_HINT_MY_STATUS_NOTIFY, &CmdAliveNoti::create);
}

CreateCommandFn CommandFactory::find(String topic)
{
	Serial.println("CommandFactory::find");

	for (SimpleList<Pair>::iterator itr = factoryMap.begin(); itr != factoryMap.end();)
	{
		Pair pair = *itr;

		if (topic.indexOf(pair.hint) >= 0)
		{
			Serial.print("fount hint: ");
			Serial.println(pair.hint);
			delay(1000);
			return (pair.pfnCreate);
		}

		++itr;
	}

	return NULL;
}

void CommandFactory::add(char *hint, CreateCommandFn pfnCreate)
{
	Pair p(hint, pfnCreate);

	factoryMap.push_back(p);
}

CommandFactory::CommandFactory()
{
	init();
}

Command * CommandFactory::createCommand(const String topic)
{
	Serial.print("CommandFactory::createCommand : ");
	Serial.println(topic);

	CreateCommandFn pCreateFnc = find(topic);

	if (pCreateFnc == NULL) {
		Serial.println("pCreateFnc is null");
		return new Command();
	}
	
	Serial.println("pCreateFnc found");
	delay(200);

	return (pCreateFnc)();
}