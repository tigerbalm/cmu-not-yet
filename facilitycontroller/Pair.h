// Pair.h

#ifndef _PAIR_h
#define _PAIR_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "Command.h"

typedef Command* (*CreateCommandFn)();

class Pair {	
public:
	String hint;
	CreateCommandFn pfnCreate;

	Pair() {}
	Pair(char* hint, CreateCommandFn pfnCreate);
};

#endif

