// NetworkManagerListener.h

#ifndef _NETWORKMANAGERLISTENER_h
#define _NETWORKMANAGERLISTENER_h

#include <WString.h>

class NetworkManagerListener 
{
public:
	virtual void onMessageReceived(String message) = 0;
};
#endif

