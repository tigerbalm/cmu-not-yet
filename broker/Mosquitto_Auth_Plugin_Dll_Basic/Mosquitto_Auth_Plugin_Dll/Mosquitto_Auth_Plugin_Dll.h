// Mosquitto_Auth_Plugin_Dll.h : main header file for the Mosquitto_Auth_Plugin_Dll DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error "include 'stdafx.h' before including this file for PCH"
#endif

#include "resource.h"		// main symbols


// CMosquitto_Auth_Plugin_DllApp
// See Mosquitto_Auth_Plugin_Dll.cpp for the implementation of this class
//
extern "C" int mosquitto_auth_plugin_version(void);
extern "C" int mosquitto_auth_plugin_init(void **user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count);
extern "C" int mosquitto_auth_plugin_cleanup(void *user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count);
extern "C" int mosquitto_auth_security_init(void *user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count, bool reload);
extern "C" int mosquitto_auth_security_cleanup(void *user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count, bool reload);
extern "C" int mosquitto_auth_acl_check(void *user_data, const char *clientid, const char *username, const char *topic, int access);
extern "C" int mosquitto_auth_unpwd_check(void *user_data, const char *username, const char *password);
extern "C" int mosquitto_auth_psk_key_get(void *user_data, const char *hint, const char *identity, char *key, int max_key_len);

class CMosquitto_Auth_Plugin_DllApp : public CWinApp
{
public:
	CMosquitto_Auth_Plugin_DllApp();

// Overrides
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};
