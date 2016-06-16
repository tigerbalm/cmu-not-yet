// 
// 
// 

#include "CommandFactory.h"
#include "CmdVerifyReservationRes.h"

//String COMMAND_VERIFY_RESERVATION("confirm_reservation");
String COMMAND_REPORT_SLOT_STATUS("");
String COMMAND_REQUEST_PAYMENT("");

Command CommandFactory::get(String command)
{
	if (command.indexOf("confirm_reservation"))
	{
		return CmdVerifyReservationRes();
	}
	
	/*
	if (command.indexOf(COMMAND_VERIFY_RESERVATION) >= 0)
	{
		return CommandVerifyReservation();
	}
	else if (command.indexOf(COMMAND_REPORT_SLOT_STATUS) >= 0)
	{
		return CommandReportSlotStatus();
	}
	else if (command.indexOf(COMMAND_REQUEST_PAYMENT) >= 0)
	{
		return CommandRequestPayment();
	}
	*/
	
	return Command();
}
