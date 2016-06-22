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
#include "CmdExceptionNoti.h"
#include "CmdReceiveBookingNumber.h"

void CommandFactory::init()
{
	add(CMD_HINT_CONFIRM_RESERVATION_REQ, &CmdVerifyBookingReq::create);
	add(CMD_HINT_CONFIRM_RESERVATION_RESP, &CmdVerifyBookingResp::create);
	
	add(CMD_HINT_PAYMENT_REQ, &CmdPaymentReq::create);
	add(CMD_HINT_PAYMENT_RESP, &CmdPaymentResp::create);

	add(CMD_HINT_CAR_PARKED_NOTIFY, &CmdCarParkedNoti::create);
	add(CMD_HINT_MY_STATUS_NOTIFY, &CmdAliveNoti::create);

	add(CMD_HINT_EXCEPTION_NOTIFY, &CmdExceptionNoti::create);

	add(CMD_HINT_RECEIVE_BOOK_NO, &CmdReceiveBookingNumber::create);
}

CreateCommandFn CommandFactory::find(String topic)
{
	for (SimpleList<Pair>::iterator itr = factoryMap.begin(); itr != factoryMap.end();)
	{
		Pair pair = *itr;

		if (topic.indexOf(pair.hint) >= 0)
		{			
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
	CreateCommandFn pCreateFnc = find(topic);

	if (pCreateFnc == NULL) {
		Serial.println("pCreateFnc is null");
		return NULL;
	}

	return (pCreateFnc)();
}