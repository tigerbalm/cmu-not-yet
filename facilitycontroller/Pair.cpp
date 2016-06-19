// 
// 
// 

#include "Pair.h"

Pair::Pair(char *_hint, CreateCommandFn _pfnCreate)
{
	hint = _hint;
	pfnCreate = _pfnCreate;
}
