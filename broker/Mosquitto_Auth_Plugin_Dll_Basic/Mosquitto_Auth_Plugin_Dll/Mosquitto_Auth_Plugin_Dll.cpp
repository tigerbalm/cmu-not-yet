// Mosquitto_Auth_Plugin_Dll.cpp : Defines the initialization routines for the DLL.
//

#include "stdafx.h"
#include "Mosquitto_Auth_Plugin_Dll.h"
#include <mosquitto.h>
#include <mosquitto_plugin.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//
//TODO: If this DLL is dynamically linked against the MFC DLLs,
//		any functions exported from this DLL which call into
//		MFC must have the AFX_MANAGE_STATE macro added at the
//		very beginning of the function.
//
//		For example:
//
//		extern "C" BOOL PASCAL EXPORT ExportedFunction()
//		{
//			AFX_MANAGE_STATE(AfxGetStaticModuleState());
//			// normal function body here
//		}
//
//		It is very important that this macro appear in each
//		function, prior to any calls into MFC.  This means that
//		it must appear as the first statement within the 
//		function, even before any object variable declarations
//		as their constructors may generate calls into the MFC
//		DLL.
//
//		Please see MFC Technical Notes 33 and 58 for additional
//		details.
//
// CMosquitto_Auth_Plugin_DllApp

BEGIN_MESSAGE_MAP(CMosquitto_Auth_Plugin_DllApp, CWinApp)
END_MESSAGE_MAP()


// CMosquitto_Auth_Plugin_DllApp construction

CMosquitto_Auth_Plugin_DllApp::CMosquitto_Auth_Plugin_DllApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CMosquitto_Auth_Plugin_DllApp object

CMosquitto_Auth_Plugin_DllApp theApp;


// CMosquitto_Auth_Plugin_DllApp initialization

BOOL CMosquitto_Auth_Plugin_DllApp::InitInstance()
{
	CWinApp::InitInstance();

	return TRUE;
}



extern "C" int  mosquitto_auth_plugin_version(void)
{
	return MOSQ_AUTH_PLUGIN_VERSION;
}


extern "C" int  mosquitto_auth_plugin_init(void **user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count)
{
	return MOSQ_ERR_SUCCESS;
}

extern "C" int  mosquitto_auth_plugin_cleanup(void *user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count)
{
	return MOSQ_ERR_SUCCESS;
}

extern "C" int  mosquitto_auth_security_init(void *user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count, bool reload)
{
	return MOSQ_ERR_SUCCESS;
}

extern "C" int  mosquitto_auth_security_cleanup(void *user_data, struct mosquitto_auth_opt *auth_opts, int auth_opt_count, bool reload)
{
	return MOSQ_ERR_SUCCESS;
}

extern "C" int  mosquitto_auth_acl_check(void *user_data, const char *clientid, const char *username, const char *topic, int access)
{
	if(!strcmp(username, "readonly") && access == MOSQ_ACL_READ){
		return MOSQ_ERR_SUCCESS;
	}else if(!strcmp(username, "writeonly") && access == MOSQ_ACL_WRITE){
		return MOSQ_ERR_SUCCESS;
	}else if(!strcmp(username, "readwrite") && (access == MOSQ_ACL_READ || access == MOSQ_ACL_WRITE)){
		return MOSQ_ERR_SUCCESS;
	}else{
		return MOSQ_ERR_ACL_DENIED;
	}
}

extern "C" int  mosquitto_auth_unpwd_check(void *user_data, const char *username, const char *password)
{
	if(!strcmp(username, "test-username") && password && !strcmp(password, "cnwTICONIURW")){
		return MOSQ_ERR_SUCCESS;
	}else if(!strcmp(username, "readonly") || !strcmp(username, "writeonly") || !strcmp(username, "readwrite")){
		return MOSQ_ERR_SUCCESS;
	}else{
		return MOSQ_ERR_AUTH;
	}
}

extern "C" int  mosquitto_auth_psk_key_get(void *user_data, const char *hint, const char *identity, char *key, int max_key_len)
{
	return MOSQ_ERR_AUTH;
}

